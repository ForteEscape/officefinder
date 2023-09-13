package com.dokkebi.officefinder.controller.lease;

import com.dokkebi.officefinder.controller.lease.dto.LeaseControllerDto.AgentLeaseLookUpResponse;
import com.dokkebi.officefinder.controller.lease.dto.LeaseControllerDto.LeaseOfficeRequest;
import com.dokkebi.officefinder.controller.lease.dto.LeaseControllerDto.LeaseSuccessResponse;
import com.dokkebi.officefinder.security.TokenProvider;
import com.dokkebi.officefinder.service.lease.LeaseService;
import com.dokkebi.officefinder.service.lease.dto.LeaseServiceDto.LeaseLookUpServiceResponse;
import com.dokkebi.officefinder.service.lease.dto.LeaseServiceDto.LeaseOfficeRequestDto;
import com.dokkebi.officefinder.service.lease.dto.LeaseServiceDto.LeaseOfficeServiceResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LeaseController {

  private final LeaseService leaseService;
  private final TokenProvider tokenProvider;

  @Operation(summary = "임대 진행", description = "임대를 진행한다.")
  @PreAuthorize("hasRole('CUSTOMER')")
  @PostMapping("/offices/{officeId}")
  public LeaseSuccessResponse leaseOffice(Principal principal, @PathVariable Long officeId,
      @RequestBody LeaseOfficeRequest req) {

    LeaseOfficeServiceResponse serviceResponse = leaseService.leaseOffice(
        LeaseOfficeRequestDto.of(principal.getName(), officeId, req));

    return LeaseSuccessResponse.of(serviceResponse);
  }

  @Operation(summary = "임대 정보 조회", description = "임대 정보를 조회할 수 있다.")
  @PreAuthorize("hasRole('CUSTOMER')")
  @GetMapping("/customers/info/leases")
  public Page<LeaseLookUpServiceResponse> getLeaseInfo(Principal principal,
      Pageable pageableReceived) {

    Pageable pageable = createPageable(pageableReceived, Sort.by(Sort.Direction.DESC, "createdAt"));

    return leaseService.getLeaseList(principal.getName(), pageable);
  }

  @Operation(summary = "임대 정보 상세 조회", description = "임대 정보를 조회할 수 있다.")
  @PreAuthorize("hasRole('CUSTOMER')")
  @GetMapping("/customers/info/leases/{leaseId}")
  public LeaseLookUpServiceResponse getLeaseDetailInfo(@RequestHeader("Authorization") String jwt,
      @PathVariable("leaseId") Long leaseId) {

    Long customerId = tokenProvider.getUserIdFromHeader(jwt);

    return leaseService.getLeaseInfo(customerId, leaseId);
  }

  @Operation(summary = "오피스에 신청된 임대 요청 조회", description = "자신의 오피스에 신청된 임대 요청들을 조회할 수 있다.")
  @PreAuthorize("hasRole('OFFICE_OWNER')")
  @GetMapping("/agents/offices/{officeId}/lease-requests")
  public Page<AgentLeaseLookUpResponse> getLeaseRequest(Principal principal,
      @PathVariable Long officeId, Pageable pageableReceived) {

    Pageable pageable = createPageable(pageableReceived, Sort.by("createdAt"));

    return leaseService.getLeaseRequestList(principal.getName(), officeId, pageable);
  }

  @Operation(summary = "오피스에 신청된 임대 수락", description = "자신의 오피스에 신청된 임대 요청을 수락한다.")
  @PreAuthorize("hasRole('OFFICE_OWNER')")
  @PutMapping("/agents/offices/lease-requests/{leaseId}/accept")
  public ResponseEntity<Object> acceptRequest(Principal principal, @PathVariable Long leaseId) {
    leaseService.acceptLeaseRequest(leaseId);

    return ResponseEntity.ok().build();
  }

  @Operation(summary = "오피스에 신청된 임대 거절", description = "자신의 오피스에 신청된 임대 요청을 거절한다.")
  @PreAuthorize("hasRole('OFFICE_OWNER')")
  @PutMapping("/agents/offices/lease-requests/{leaseId}/reject")
  public ResponseEntity<Object> rejectRequest(Principal principal, @PathVariable Long leaseId) {

    leaseService.rejectLeaseRequest(leaseId);

    return ResponseEntity.ok().build();
  }

  private Pageable createPageable(Pageable pageableReceived, Sort sort) {
    return PageRequest.of(pageableReceived.getPageNumber(), pageableReceived.getPageSize(), sort);
  }
}
