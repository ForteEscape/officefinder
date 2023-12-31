package com.dokkebi.officefinder.controller.notification;

import com.dokkebi.officefinder.service.notification.NotificationService;
import com.dokkebi.officefinder.service.notification.dto.NotificationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  // 클라이언트의 SSE 연결 요청을 처리하는 엔드 포인트
  // produces 속성에 text/event-stream을 설정하여 이 엔드포인트가 SSE 연결을 위한 것임을 나타냄
  @GetMapping(value = "/sub", produces = "text/event-stream")
  public SseEmitter subscribe(Principal principal,
      // 만약 연결이 끊기거나 다른 이유로 이벤트를 받지 못하게 된다면, 브라우저는 다시 연결을 시도할때
      // 이 Last-Event-ID 헤더를 포함하여 어디서부터 데이터를 다시 받아야 하는지 서버에 알림
      @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {

    return notificationService.subscribe(principal.getName(), lastEventId);
  }

  @Operation(summary = "알림 리스트 조회", description = "임대 업자가 본인에게 도착한 알림 내역 조회")
  @PreAuthorize("hasRole('OFFICE_OWNER')")
  @GetMapping("/api/agents/notifications")
  public Page<NotificationResponseDto> getNotificationListByOwner(Principal principal,
      Pageable pageableReceived) {
    Pageable pageable = createPageable(pageableReceived, Sort.by(Sort.Direction.DESC, "createdAt"));

    return notificationService.getNotificationByOwner(
        principal.getName(), pageable);
  }

  @Operation(summary = "알림 리스트 조회", description = "고객이 본인에게 도착한 알림 내역 조회")
  @PreAuthorize("hasRole('CUSTOMER')")
  @GetMapping("/api/customers/notifications")
  public Page<NotificationResponseDto> getNotificationListByCustomer(Principal principal,
      Pageable pageableReceived) {

    Pageable pageable = createPageable(pageableReceived, Sort.by(Sort.Direction.DESC, "createdAt"));

    return notificationService.getNotificationByCustomer(
        principal.getName(), pageable);
  }

  private Pageable createPageable(Pageable pageableReceived, Sort sort) {
    return PageRequest.of(pageableReceived.getPageNumber(), pageableReceived.getPageSize(), sort);
  }
}
