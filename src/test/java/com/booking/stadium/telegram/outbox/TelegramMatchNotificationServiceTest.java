package com.booking.stadium.telegram.outbox;

import com.booking.stadium.telegram.config.TelegramProperties;
import com.booking.stadium.telegram.event.MatchRequestCreatedEvent;
import com.booking.stadium.telegram.subscription.TelegramSubscription;
import com.booking.stadium.telegram.subscription.TelegramSubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramMatchNotificationServiceTest {

    @Mock
    private TelegramSubscriptionService subscriptionService;
    @Mock
    private TelegramNotificationOutboxRepository outboxRepository;

    @Test
    void enqueueMatchCreated_shouldCreateOutboxForSubscribers() {
        TelegramProperties properties = new TelegramProperties();
        properties.setBotEnabled(true);

        TelegramMatchNotificationService service = new TelegramMatchNotificationService(
                subscriptionService, outboxRepository, new ObjectMapper().findAndRegisterModules(), properties);

        when(subscriptionService.getActiveSubscribers()).thenReturn(List.of(
                TelegramSubscription.builder().chatId(1001L).build(),
                TelegramSubscription.builder().chatId(1002L).build()
        ));

        MatchRequestCreatedEvent event = MatchRequestCreatedEvent.builder()
                .eventId("evt-1")
                .matchRequestId(1L)
                .matchCode("M001")
                .stadiumName("Sân A")
                .fieldName("Sân 7")
                .bookingDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(19, 0))
                .endTime(LocalTime.of(20, 30))
                .hostTeamName("Team A")
                .contactPhone("+84911111111")
                .build();

        service.enqueueMatchCreated(event);

        ArgumentCaptor<TelegramNotificationOutbox> captor = ArgumentCaptor.forClass(TelegramNotificationOutbox.class);
        verify(outboxRepository, times(2)).save(captor.capture());
        assertEquals("evt-1", captor.getAllValues().get(0).getEventId());
    }
}
