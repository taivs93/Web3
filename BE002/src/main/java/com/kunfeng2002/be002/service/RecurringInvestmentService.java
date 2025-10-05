package com.kunfeng2002.be002.service;

import com.kunfeng2002.be002.dto.request.RecurringInvestmentRequest;
import com.kunfeng2002.be002.dto.response.RecurringInvestmentResponse;
import com.kunfeng2002.be002.entity.Chat;
import com.kunfeng2002.be002.entity.InvestmentFrequency;
import com.kunfeng2002.be002.entity.RecurringInvestment;
import com.kunfeng2002.be002.entity.User;
import com.kunfeng2002.be002.event.TelegramMessageEvent;
import com.kunfeng2002.be002.exception.DataNotFoundException;
import com.kunfeng2002.be002.repository.ChatRepository;
import com.kunfeng2002.be002.repository.RecurringInvestmentRepository;
import com.kunfeng2002.be002.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringInvestmentService {

    private final RecurringInvestmentRepository recurringInvestmentRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public RecurringInvestmentResponse createInvestment(Long userId, RecurringInvestmentRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        LocalDateTime nextDate = calculateNextNotificationDate(request.getFrequency(), request.getNotificationTime());

        RecurringInvestment investment = RecurringInvestment.builder()
                .userId(userId)
                .coinSymbol(request.getCoinSymbol())
                .coinAddress(request.getCoinAddress())
                .amount(request.getAmount())
                .frequency(request.getFrequency())
                .notificationTime(request.getNotificationTime())
                .nextNotificationDate(nextDate)
                .isActive(true)
                .build();

        investment = recurringInvestmentRepository.save(investment);
        return mapToResponse(investment);
    }

    public List<RecurringInvestmentResponse> getUserInvestments(Long userId) {
        return recurringInvestmentRepository.findByUserIdAndIsActiveTrue(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecurringInvestmentResponse updateInvestment(Long userId, Long investmentId, RecurringInvestmentRequest request) {
        RecurringInvestment investment = recurringInvestmentRepository.findById(investmentId)
                .orElseThrow(() -> new DataNotFoundException("Investment not found"));

        if (!investment.getUserId().equals(userId)) {
            throw new DataNotFoundException("Investment not found");
        }

        investment.setCoinSymbol(request.getCoinSymbol());
        investment.setCoinAddress(request.getCoinAddress());
        investment.setAmount(request.getAmount());
        investment.setFrequency(request.getFrequency());
        investment.setNotificationTime(request.getNotificationTime());
        investment.setNextNotificationDate(calculateNextNotificationDate(request.getFrequency(), request.getNotificationTime()));

        investment = recurringInvestmentRepository.save(investment);
        return mapToResponse(investment);
    }

    @Transactional
    public void deleteInvestment(Long userId, Long investmentId) {
        RecurringInvestment investment = recurringInvestmentRepository.findById(investmentId)
                .orElseThrow(() -> new DataNotFoundException("Investment not found"));

        if (!investment.getUserId().equals(userId)) {
            throw new DataNotFoundException("Investment not found");
        }

        investment.setIsActive(false);
        recurringInvestmentRepository.save(investment);
    }

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void sendScheduledNotifications() {
        log.info("Checking for due recurring investment notifications");
        LocalDateTime now = LocalDateTime.now();
        List<RecurringInvestment> dueInvestments = recurringInvestmentRepository.findDueInvestments(now);

        for (RecurringInvestment investment : dueInvestments) {
            try {
                Chat chat = chatRepository.findByUserId(investment.getUserId()).orElse(null);
                if (chat == null) {
                    log.warn("No chat found for user {} (investment {}). User needs to link Telegram first.", 
                        investment.getUserId(), investment.getId());
                    continue;
                }

                String message = buildNotificationMessage(investment);
                eventPublisher.publishEvent(new TelegramMessageEvent(chat.getChatId(), message));
                log.info("Sent notification to chatId {} for investment {}", chat.getChatId(), investment.getId());

                investment.setNextNotificationDate(calculateNextRecurringDate(investment.getNextNotificationDate(), investment.getFrequency()));
                recurringInvestmentRepository.save(investment);
            } catch (Exception e) {
                log.error("Error sending notification for investment {}: {}", investment.getId(), e.getMessage(), e);
            }
        }
    }

    private String buildNotificationMessage(RecurringInvestment investment) {
        String frequencyText;
        switch (investment.getFrequency()) {
            case WEEKLY:
                frequencyText = "hàng tuần";
                break;
            case MONTHLY:
                frequencyText = "hàng tháng";
                break;
            case QUARTERLY:
                frequencyText = "hàng quý";
                break;
            default:
                frequencyText = "";
        }

        return String.format(
                "Nhắc nhở đầu tư %s\n\n" +
                "Coin: %s\n" +
                "Số tiền: %s\n" +
                "Kỳ hạn: %s\n\n" +
                "Đây là lời nhắc để bạn thực hiện khoản đầu tư định kỳ của mình.",
                frequencyText,
                investment.getCoinSymbol(),
                investment.getAmount().toString(),
                frequencyText
        );
    }

    private LocalDateTime calculateNextNotificationDate(InvestmentFrequency frequency, java.time.LocalTime notificationTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDate = now.toLocalDate().atTime(notificationTime);
        
        if (nextDate.isBefore(now) || nextDate.isEqual(now)) {
            nextDate = nextDate.plusDays(1);
        }
        
        return nextDate;
    }
    
    private LocalDateTime calculateNextRecurringDate(LocalDateTime currentDate, InvestmentFrequency frequency) {
        switch (frequency) {
            case WEEKLY:
                return currentDate.plusWeeks(1);
            case MONTHLY:
                return currentDate.plusMonths(1);
            case QUARTERLY:
                return currentDate.plusMonths(3);
            default:
                return currentDate.plusMonths(1);
        }
    }

    private RecurringInvestmentResponse mapToResponse(RecurringInvestment investment) {
        return RecurringInvestmentResponse.builder()
                .id(investment.getId())
                .coinSymbol(investment.getCoinSymbol())
                .coinAddress(investment.getCoinAddress())
                .amount(investment.getAmount())
                .frequency(investment.getFrequency())
                .notificationTime(investment.getNotificationTime())
                .nextNotificationDate(investment.getNextNotificationDate())
                .isActive(investment.getIsActive())
                .createdAt(investment.getCreatedAt())
                .build();
    }
}

