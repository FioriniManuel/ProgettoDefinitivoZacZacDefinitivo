package com.ispw.progettoispw.Controller.ControllerApplicativo;

import com.ispw.progettoispw.ApplicationFacade.ApplicationFacade;
import com.ispw.progettoispw.Dao.GenericDao;
import com.ispw.progettoispw.Dao.ReadOnlyDao;
import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.Enum.GenderCategory;
import com.ispw.progettoispw.Enum.PaymentChannel;
import com.ispw.progettoispw.Exception.BusinessRuleException;
import com.ispw.progettoispw.Exception.ConflittoPrenotazioneException;
import com.ispw.progettoispw.Exception.OggettoInvalidoException;
import com.ispw.progettoispw.Exception.ValidazioneException;
import com.ispw.progettoispw.Factory.DaoFactory;
import com.ispw.progettoispw.Session.SessionManager;
import com.ispw.progettoispw.bean.BarbiereBean;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean;
import com.ispw.progettoispw.entity.Appuntamento;
import com.ispw.progettoispw.entity.Barbiere;
import com.ispw.progettoispw.entity.Servizio;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BookingController {

    private static final LocalTime MORN_START = LocalTime.of(8, 0);
    private static final LocalTime MORN_END   = LocalTime.of(13, 0);
    private static final LocalTime AFT_START  = LocalTime.of(14, 0);
    private static final LocalTime AFT_END    = LocalTime.of(20, 0);
    private static final int STEP_MIN = 30;

    private static final DecimalFormat PRICE_FMT = new DecimalFormat("#,##0.00");

    private final ReadOnlyDao<Servizio> servizioDao;
    private final GenericDao<Barbiere> barbiereDao;
    private final GenericDao<Appuntamento> appuntamentoDao;

    public BookingController() {
        DaoFactory factory = DaoFactory.getInstance();
        this.servizioDao     = factory.getServizioDao();
        this.barbiereDao     = factory.getBarbiereDao();
        this.appuntamentoDao = factory.getAppuntamentoDao();
    }

    // ===================== SERVIZI =====================

    public List<Servizio> listServiziByCategory(GenderCategory cat) {
        List<Servizio> all = servizioDao.readAll();
        if (all == null) return List.of();
        if (cat == null) return all;

        List<Servizio> out = new ArrayList<>();
        for (Servizio s : all) {
            if (s != null && s.getCategory() == cat) out.add(s);
        }
        return out;
    }

    public String buildServiceLabel(Servizio s) {
        BigDecimal price = (s.getPrice() == null) ? BigDecimal.ZERO : s.getPrice();
        return s.getName() + "  [€ " + PRICE_FMT.format(price) + "]";
    }

    public Servizio getServizio(String id) {
        return (id == null) ? null : servizioDao.read(id);
    }

    // ===================== BARBIERI =====================

    public List<Barbiere> listBarbersByGender(GenderCategory cat) {
        if (cat == null) return List.of();
        return barbiereDao.readAll().stream()
                .filter(Barbiere::isActive)
                .filter(b -> b.getSpecializzazione() == cat)
                .toList();
    }

    // ===================== VM MAPPER =====================

    private ServizioBean toVM(Servizio s) {
        if (s == null) return null;
        return new ServizioBean(s.getServiceId(), s.getName(), s.getPrice(), s.getDuration());
    }

    private BarbiereBean toVM(Barbiere b) {
        if (b == null) return null;
        String fn = (b.getFirstName() == null) ? "" : b.getFirstName();
        String ln = (b.getLastName() == null) ? "" : b.getLastName();
        String full = (fn + " " + ln).trim();
        if (full.isEmpty()) {
            full = (b.getEmail() != null && !b.getEmail().isBlank()) ? b.getEmail() : b.getId();
        }
        return new BarbiereBean(b.getId(), full, b.getEmail());
    }

    // ===================== API VM =====================

    public List<ServizioBean> listServiziByCategoryVM(GenderCategory cat) {
        return listServiziByCategory(cat).stream()
                .map(this::toVM)
                .filter(Objects::nonNull)
                .toList();
    }

    public String buildServiceLabel(ServizioBean s) {
        BigDecimal price = (s.getPrice() == null) ? BigDecimal.ZERO : s.getPrice();
        return s.getName() + "  [€ " + PRICE_FMT.format(price) + "]";
    }

    public List<BarbiereBean> listBarbersByGenderVM(GenderCategory cat) {
        return listBarbersByGender(cat).stream()
                .map(this::toVM)
                .filter(Objects::nonNull)
                .toList();
    }

    public ServizioBean getServizioVM(String id) {
        Servizio s = getServizio(id);
        return (s == null) ? null : toVM(s);
    }

    // ===================== DISPONIBILITÀ =====================

    public List<BarbiereBean> availableBarbersVM(LocalDate day, String servizioId) {
        return availableBarbers(day, servizioId).stream()
                .map(this::toVM)
                .toList();
    }

    public List<Barbiere> availableBarbers(LocalDate day, String servizioId) {
        if (day == null || servizioId == null || servizioId.isBlank()) return List.of();

        Servizio s = servizioDao.read(servizioId);
        if (s == null) return List.of();

        GenderCategory cat = s.getCategory();
        int durata = s.getDuration();
        if (durata <= 0) return List.of();

        List<Barbiere> candidates = barbiereDao.readAll().stream()
                .filter(Barbiere::isActive)
                .filter(b -> b.getSpecializzazione() == cat)
                .toList();

        List<Barbiere> out = new ArrayList<>();
        for (Barbiere b : candidates) {
            if (!listAvailableStartTimesWithoutService(b.getId(), day).isEmpty()) {
                out.add(b);
            }
        }
        return out;
    }

    // ===================== ORARI =====================

    public List<LocalTime> listAvailableStartTimes(String barbiereId, LocalDate day, String servizioId) {
        if (barbiereId == null || day == null || servizioId == null) return List.of();

        Servizio servizio = servizioDao.read(servizioId);
        if (servizio == null) return List.of();

        int durata = servizio.getDuration();
        if (durata <= 0) return List.of();

        List<Appuntamento> booked = appuntamentoDao.readAll().stream()
                .filter(a -> barbiereId.equals(a.getBarberId()))
                .filter(a -> day.equals(a.getDate()))
                .toList();

        List<LocalTime> candidates = new ArrayList<>();
        candidates.addAll(generateStarts(MORN_START, MORN_END, durata));
        candidates.addAll(generateStarts(AFT_START, AFT_END, durata));

        List<LocalTime> out = new ArrayList<>();
        for (LocalTime t : candidates) {
            if (isFreeInterval(t, t.plusMinutes(durata), booked)) out.add(t);
        }
        return out;
    }

    private List<LocalTime> listAvailableStartTimesWithoutService(String barbiereId, LocalDate day) {
        if (barbiereId == null || day == null) return List.of();

        List<Appuntamento> booked = appuntamentoDao.readAll().stream()
                .filter(a -> barbiereId.equals(a.getBarberId()))
                .filter(a -> day.equals(a.getDate()))
                .toList();

        List<LocalTime> candidates = new ArrayList<>();
        candidates.addAll(generateStarts(MORN_START, MORN_END, STEP_MIN));
        candidates.addAll(generateStarts(AFT_START, AFT_END, STEP_MIN));

        List<LocalTime> out = new ArrayList<>();
        for (LocalTime t : candidates) {
            if (isFreeInterval(t, t.plusMinutes(STEP_MIN), booked)) out.add(t);
        }
        return out;
    }

    private List<LocalTime> generateStarts(LocalTime from, LocalTime to, int durataMin) {
        List<LocalTime> out = new ArrayList<>();
        for (LocalTime t = from; !t.plusMinutes(durataMin).isAfter(to); t = t.plusMinutes(STEP_MIN)) {
            out.add(t);
        }
        return out;
    }

    private boolean isFreeInterval(LocalTime startT, LocalTime endT, List<Appuntamento> existing) {
        for (Appuntamento a : existing) {
            if (overlap(startT, endT, a.getSlotIndex(), a.getSlotFin())) return false;
        }
        return true;
    }

    private boolean overlap(LocalTime s1, LocalTime e1, LocalTime s2, LocalTime e2) {
        return s1.isBefore(e2) && s2.isBefore(e1);
    }

    // ===================== BOOKING (NUOVO con eccezioni) =====================

    public void book(BookingBean bean)
            throws ValidazioneException, OggettoInvalidoException, ConflittoPrenotazioneException {

        if (bean == null) throw new ValidazioneException("Dati prenotazione mancanti.");

        String servizioId = bean.getServizioId();
        if (servizioId == null || servizioId.isBlank())
            throw new ValidazioneException("Seleziona un servizio.");

        Servizio servizio = servizioDao.read(servizioId);
        if (servizio == null)
            throw new OggettoInvalidoException("Servizio inesistente.");

        bean.setDurataTotaleMin(servizio.getDuration());
        bean.computeEndTime();

        List<String> errs = bean.validate();
        if (!errs.isEmpty())
            throw new ValidazioneException(String.join("\n", errs));

        List<LocalTime> free = listAvailableStartTimes(bean.getBarbiereId(), bean.getDay(), servizioId);
        if (free.stream().noneMatch(t -> t.equals(bean.getStartTime())))
            throw new ConflittoPrenotazioneException("Slot non disponibile, scegli un altro orario.");

        Appuntamento a = Appuntamento.newWithId();
        a.setServizio(bean.getServizioId());
        a.setClientId(bean.getClienteId());
        a.setBarberId(bean.getBarbiereId());
        a.setDate(bean.getDay());
        a.setSlotIndex(bean.getStartTime());
        a.setSlotFin(bean.getEndTime());
        a.setBaseAmount(bean.getPrezzoTotale());
        a.setAppliedCouponCode(bean.getCouponCode());

        if (bean.getCanale() == PaymentChannel.IN_SHOP) {
            a.setInsede();
            a.setStatus(AppointmentStatus.PENDING);
        } else {
            a.setStatus(AppointmentStatus.CONFIRMED);
            a.setOnline();
        }

        appuntamentoDao.create(a);
    }

    /**
     * Solo verifica disponibilità (per flusso pagamento online), NON crea.
     */
    public void bookOnlineCheck(BookingBean bean)
            throws ValidazioneException, OggettoInvalidoException, ConflittoPrenotazioneException {

        if (bean == null) throw new ValidazioneException("Dati prenotazione mancanti.");

        String servizioId = bean.getServizioId();
        if (servizioId == null || servizioId.isBlank())
            throw new ValidazioneException("Seleziona un servizio.");

        Servizio servizio = servizioDao.read(servizioId);
        if (servizio == null)
            throw new OggettoInvalidoException("Servizio inesistente.");

        bean.setDurataTotaleMin(servizio.getDuration());
        bean.computeEndTime();

        List<String> errs = bean.validate();
        if (!errs.isEmpty())
            throw new ValidazioneException(String.join("\n", errs));

        List<LocalTime> free = listAvailableStartTimes(bean.getBarbiereId(), bean.getDay(), servizioId);
        if (free.stream().noneMatch(t -> t.equals(bean.getStartTime())))
            throw new ConflittoPrenotazioneException("Slot non disponibile, scegli un altro orario.");
    }

    // ===================== SESSION BOOKING =====================

    public void saveBookingToSession(BookingBean bean) {
        SessionManager.getInstance().setCurrentBooking(bean);
    }

    public BookingBean getBookingFromSession() {
        return SessionManager.getInstance().getCurrentBooking();
    }

    public void clearBookingFromSession() {
        SessionManager.getInstance().clearCurrentBooking();
    }

    // ===================== LISTE APPUNTAMENTI =====================

    public List<BookingBean> listCustomerAppointmentsVM(String clientId) {
        if (clientId == null) return List.of();

        return appuntamentoDao.readAll().stream()
                .filter(a -> clientId.equals(a.getClientId()))
                .sorted(Comparator.comparing(Appuntamento::getDate)
                        .thenComparing(Appuntamento::getSlotIndex))
                .map(a -> {
                    BookingBean b = new BookingBean();
                    b.setAppointmentId(a.getId());
                    b.setClienteId(a.getClientId());
                    b.setBarbiereId(a.getBarberId());
                    b.setDay(a.getDate());
                    b.setStartTime(a.getSlotIndex());
                    b.setEndTime(a.getSlotFin());
                    b.setPrezzoTotale(a.getBaseAmount());
                    b.setStatus(a.getStatus());
                    b.setCouponCode(a.getAppliedCouponCode());
                    b.setCanale(a.getPaymentChannel());

                    b.setServiziId(a.getServizio());
                    Servizio s = servizioDao.read(a.getServizio());
                    if (s != null) {
                        b.setServiceName(s.getName());
                        b.setCategoria(s.getCategory());
                        b.setDurataTotaleMin(s.getDuration());
                    }
                    return b;
                })
                .toList();
    }

    public List<BookingBean> listAppointmentsForBarberOnDayVM(String barberId, LocalDate day) {
        if (barberId == null || day == null) return List.of();

        List<Appuntamento> entityList = appuntamentoDao.readAll().stream()
                .filter(a -> barberId.equals(a.getBarberId()))
                .filter(a -> day.equals(a.getDate()))
                .toList();

        List<BookingBean> beans = new ArrayList<>();
        for (Appuntamento a : entityList) {
            BookingBean b = new BookingBean();
            b.setClienteId(a.getClientId());
            b.setBarbiereId(a.getBarberId());
            b.setDay(a.getDate());
            b.setStartTime(a.getSlotIndex());
            b.setEndTime(a.getSlotFin());
            b.setDurataTotaleMin((int) Duration.between(a.getSlotIndex(), a.getSlotFin()).toMinutes());
            b.setPrezzoTotale(a.getBaseAmount());

            b.setAppointmentId(a.getId());
            b.setStatus(a.getStatus());

            Servizio s = servizioDao.read(a.getServizio());
            if (s != null) {
                b.setServiziId(s.getServiceId());
                b.setCategoria(s.getCategory());
                b.setPrezzoTotale(s.getPrice());
                b.setDurataTotaleMin(s.getDuration());
                b.setServiceName(s.getName());
            }

            beans.add(b);
        }
        return beans;
    }

    // ===================== UPDATE/CANCEL (con eccezioni) =====================

    public void updateAppointmentStatus(String appointmentId, AppointmentStatus newStatus)
            throws ValidazioneException, OggettoInvalidoException {

        if (appointmentId == null || appointmentId.isBlank())
            throw new ValidazioneException("Id appuntamento mancante.");
        if (newStatus == null)
            throw new ValidazioneException("Nuovo stato mancante.");

        Appuntamento a = appuntamentoDao.read(appointmentId);
        if (a == null)
            throw new OggettoInvalidoException("Appuntamento non trovato.");

        a.setStatus(newStatus);
        appuntamentoDao.update(a);
    }

    public void cancelCustomerAppointment(String appointmentId)
            throws ValidazioneException, OggettoInvalidoException, BusinessRuleException {

        if (appointmentId == null || appointmentId.isBlank())
            throw new ValidazioneException("Id appuntamento mancante.");

        Appuntamento a = appuntamentoDao.read(appointmentId);
        if (a == null)
            throw new OggettoInvalidoException("Appuntamento non trovato.");

        if (a.getStatus() == AppointmentStatus.CANCELLED || a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("Non puoi cancellare un appuntamento già concluso o cancellato.");
        }

        a.setStatus(AppointmentStatus.CANCELLED);
        appuntamentoDao.update(a);
    }

    // ===================== EMAIL / PAY =====================

    public void sendEmail(BookingBean bean) {
        ApplicationFacade.sendBookingEmail(bean);
    }

    public String paga(String intestatario, String cardNumber, String expire, String cvv, BigDecimal amount) {
        return ApplicationFacade.processPayment(intestatario, cardNumber, expire, cvv, amount);
    }
}
