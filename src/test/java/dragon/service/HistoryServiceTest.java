package dragon.service;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import dragon.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class HistoryServiceTest {
    HistoryService historyService;
    @BeforeEach
    void setUp() {
        TransactionRepository transactionRepository = new TransactionRepository();
        historyService = new HistoryService(transactionRepository);
    }

    @Test
    void test_getAllHistory_positive(){
        assertNotNull(historyService.getAllHistory());
    }


    //TODO: Make negative test after repository layer is done.
//    @Test
//    void test_getAllHistory_negative(){
//        AuthenticatedAccountContext.setAuthenticatedUserId(null);
//        assertFalse(historyService.getAllHistory());
//    }

    @Test
    void test_getRangeHistory_positive() {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDate startDate = LocalDate.parse("2006-09-01", dateFormatter);
        Instant startInstant = startDate.atStartOfDay(zoneId).toInstant();

        LocalDate endDate = LocalDate.parse("2016-09-01", dateFormatter);
        Instant endInstant = endDate.atTime(LocalTime.MAX.withNano(0)).atZone(zoneId).toInstant();

        assertNotNull(historyService.getRangeHistory(startInstant, endInstant));
    }

    @Test
    void test_getRangeHistory_negative() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDate startDate = LocalDate.parse("2026-09-01", dateFormatter);
        Instant startInstant = startDate.atStartOfDay(zoneId).toInstant();

        LocalDate endDate = LocalDate.parse("2016-09-01", dateFormatter);
        Instant endInstant = endDate.atTime(LocalTime.MAX.withNano(0)).atZone(zoneId).toInstant();


        assertNull(historyService.getRangeHistory(startInstant, endInstant));
    }

}
