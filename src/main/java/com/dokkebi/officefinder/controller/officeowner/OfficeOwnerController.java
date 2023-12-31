package com.dokkebi.officefinder.controller.officeowner;

import static com.dokkebi.officefinder.exception.CustomErrorCode.USER_NOT_FOUND;

import com.dokkebi.officefinder.controller.office.dto.OfficeCreateRequestDto;
import com.dokkebi.officefinder.controller.office.dto.OfficeDashboardDto;
import com.dokkebi.officefinder.controller.office.dto.OfficeModifyRequestDto;
import com.dokkebi.officefinder.controller.office.dto.OfficeNameDto;
import com.dokkebi.officefinder.controller.officeowner.dto.OfficeOwnerInfoDto;
import com.dokkebi.officefinder.controller.officeowner.dto.OfficeOwnerModifyDto;
import com.dokkebi.officefinder.controller.officeowner.dto.OfficeOwnerOverViewDto;
import com.dokkebi.officefinder.controller.officeowner.dto.OwnerOfficeOverViewDto;
import com.dokkebi.officefinder.dto.ResponseDto;
import com.dokkebi.officefinder.entity.OfficeOwner;
import com.dokkebi.officefinder.entity.office.Office;
import com.dokkebi.officefinder.entity.office.OfficePicture;
import com.dokkebi.officefinder.exception.CustomException;
import com.dokkebi.officefinder.repository.OfficeOwnerRepository;
import com.dokkebi.officefinder.repository.office.picture.OfficePictureRepository;
import com.dokkebi.officefinder.security.TokenProvider;
import com.dokkebi.officefinder.service.office.OfficeSearchService;
import com.dokkebi.officefinder.service.office.OfficeService;
import com.dokkebi.officefinder.service.officeowner.OfficeOwnerService;
import com.dokkebi.officefinder.service.officeowner.dto.OfficeOwnerServiceDto.RentalStatusDto;
import com.dokkebi.officefinder.service.review.ReviewService;
import com.dokkebi.officefinder.service.s3.S3Service;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/agents")
@Slf4j
@PreAuthorize("hasRole('OFFICE_OWNER')")
public class OfficeOwnerController {

  private final OfficeService officeService;
  private final OfficeSearchService officeQueryService;
  private final OfficePictureRepository officePictureRepository;
  private final OfficeOwnerRepository officeOwnerRepository;
  private final S3Service s3Service;
  private final OfficeOwnerService officeOwnerService;
  private final TokenProvider tokenProvider;
  private final ReviewService reviewService;

  @ApiOperation(value = "임대주 요약 정보 조회", notes = "임대주 요약 정보(이름, 역할, 사진, 포인트)를 가져올 수 있다.")
  @GetMapping("/info-overview")
  public OfficeOwnerOverViewDto getOfficeOwnerOverView(
      @RequestHeader("Authorization") String jwt) {

    return officeOwnerService.getAgentOverViewInfo(tokenProvider.getUserIdFromHeader(jwt));
  }

  @ApiOperation(value = "임대주 정보 조회", notes = "임대주 정보를 가져올 수 있다.")
  @GetMapping("/info")
  public ResponseDto<OfficeOwnerInfoDto> getOfficeOwnerInfo(@RequestHeader("Authorization") String jwt) {
    OfficeOwnerInfoDto officeOwnerInfoDto = officeOwnerService.getAgentInfo(tokenProvider.getUserIdFromHeader(jwt));
    return new ResponseDto<>("success", officeOwnerInfoDto);
  }

  @Operation(summary = "임대주 이미지 등록 및 수정", description = "임대주의 프로필 이미지를 등록하거나 수정할 수 있다.")
  @PostMapping("/info/profileImage")
  public ResponseDto<String> modifyProfileImage(@RequestPart("value") MultipartFile multipartFile,
      Principal principal) {

    OfficeOwner officeOwner = officeOwnerRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    if (!officeOwner.getOfficeOwnerProfileImage().equals("None")) {
      s3Service.deleteImages(List.of(officeOwner.getOfficeOwnerProfileImage()));
    }

    String userImagePath = s3Service.uploadImages(List.of(multipartFile)).get(0);
    officeOwnerService.changeAgentProfileImage(userImagePath, principal.getName());

    return new ResponseDto<>("success", "image modify success");
  }

  @Operation(summary = "임대주 이름 수정", description = "임대주의 이름을 수정할 수 있다.")
  @PutMapping("/info/username")
  public String changeAgentName(@RequestBody @Valid OfficeOwnerModifyDto officeOwnerModifyDto,
      @RequestHeader("Authorization") String jwt) {

    officeOwnerService.changeAgentName(officeOwnerModifyDto.getName(),
        tokenProvider.getUserIdFromHeader(jwt));

    return "success";
  }

  @Operation(summary = "임대주 프로필 이미지 초기화", description = "임대주의 프로필 이미지를 기본 이미지로 초기화한다.")
  @DeleteMapping("/info/profileImage")
  public ResponseDto<String> initProfileImage(Principal principal) {
    OfficeOwner officeOwner = officeOwnerRepository.findByEmail(principal.getName())
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

    if (!officeOwner.getOfficeOwnerProfileImage().equals("None")) {
      s3Service.deleteImages(List.of(officeOwner.getOfficeOwnerProfileImage()));
    }

    officeOwnerService.changeAgentProfileImage("None", principal.getName());

    return new ResponseDto<>("success", "image modify success");
  }

  @ApiOperation(value = "오피스 리스트 조회", notes = "자신이 등록한 오피스 리스트를 조회할 수 있다.")
  @GetMapping("/offices")
  public Page<OwnerOfficeOverViewDto> showOfficeList(Principal principal, Pageable pageable) {
    Page<Office> result = officeQueryService.getAllOffices(principal.getName(), pageable);

    return result.map(content -> OwnerOfficeOverViewDto.fromEntity(content,
        officePictureRepository.findByOfficeId(content.getId())));
  }

  @Operation(summary = "소유 오피스 이름 조회", description = "모든 오피스의 이름을 조회할 수 있다.")
  @GetMapping("/offices/names")
  public ResponseDto<List<OfficeNameDto>> getAllOfficeNames(
      @RequestHeader("Authorization") String jwt) {
    List<Office> officeList = officeQueryService.getAllOfficeName(
        tokenProvider.getUserIdFromHeader(jwt));

    List<OfficeNameDto> result = officeList.stream()
        .map(OfficeNameDto::from)
        .collect(Collectors.toList());

    result.add(0, new OfficeNameDto(-1L, "전체"));

    return new ResponseDto<>("success", result);
  }

  @ApiOperation(value = "오피스 등록", notes = "자신이 가진 오피스를 서비스에 등록할 수 있다.")
  @PostMapping("/offices")
  public void addOffice(
      @RequestPart(value = "request") OfficeCreateRequestDto request,
      @RequestPart(value = "multipartFileList", required = false) List<MultipartFile> multipartFileList,
      Principal principal
  ) {

    List<String> imagePaths;

    if (multipartFileList != null && !multipartFileList.isEmpty()){
      imagePaths = s3Service.uploadImages(multipartFileList);
    } else {
      imagePaths = new ArrayList<>();
    }

    officeService.createOfficeInfo(request, imagePaths, principal.getName());
  }

  @Operation(summary = "오피스 상세 조회", description = "자신이 등록한 오피스의 상세 정보롤 볼 수 있다.")
  @GetMapping("/offices/{officeId}")
  public OfficeDashboardDto showOfficeDetail(@PathVariable("officeId") Long officeId) {
    Office office = officeQueryService.getOfficeInfo(officeId);

    return OfficeDashboardDto.from(office);
  }

  @Operation(summary = "오피스 정보 수정", description = "자신의 오피스 정보를 수정할 수 있다.")
  @PostMapping("/offices/{officeId}")
  public void modifyOffice(
      @PathVariable("officeId") Long officeId,
      @RequestPart(value = "request") OfficeModifyRequestDto request,
      @RequestPart(value = "multipartFileList", required = false) List<MultipartFile> multipartFileList,
      Principal principal
  ) {

    // 기존 이미지 삭제
    List<OfficePicture> officePicture = officePictureRepository.findByOfficeId(officeId);

    if (officePicture != null && !officePicture.isEmpty()) {
      List<String> fileList = officePicture.stream()
          .map(OfficePicture::getFileName)
          .collect(Collectors.toList());

      s3Service.deleteImages(fileList);
    }

    List<String> imagePaths;
    if (multipartFileList != null && !multipartFileList.isEmpty()){
      imagePaths = s3Service.uploadImages(multipartFileList);
    } else{
      imagePaths = new ArrayList<>();
    }

    // 들어온 이미지 등록
    officeService.modifyOfficeInfo(request, imagePaths, principal.getName(), officeId);
  }

  @Operation(summary = "해당 오피스의 매출 조회", description = "특정 오피스의 매출을 가져올 수 있다.")
  @GetMapping("/offices/{officeId}/revenue")
  public ResponseDto<HashMap<String, Long>> getOfficeRevenue(@PathVariable Long officeId,
      @RequestHeader("Authorization") String jwt) {
    HashMap<String, Long> officeRevenue = officeOwnerService.getOfficeRevenue(officeId, jwt);

    return new ResponseDto<>("success", officeRevenue);
  }

  @Operation(summary = "오피스 전체 매출 조회", description = "임대주가 가진 모든 오피스의 매출 합을 가져올 수 있다.")
  @GetMapping("/offices/total-revenue")
  public ResponseDto<HashMap<String, Long>> getOfficesTotalRevenue(
      @RequestHeader("Authorization") String jwt) {
    HashMap<String, Long> officeRevenue = officeOwnerService.getOfficesTotalRevenue(jwt);

    return new ResponseDto<>("success", officeRevenue);
  }


  @Operation(summary = "오피스 임대 현황 조회", description = "특정 오피스의 임대 현황을 조회할 수 있다.")
  @GetMapping("/offices/{officeId}/rental-status")
  public ResponseDto<RentalStatusDto> getOfficeRentalStatus(@PathVariable Long officeId,
      @RequestHeader("Authorization") String jwt) {
    RentalStatusDto officeLeaseRate = officeOwnerService.getOfficeRentalStatus(officeId, jwt);

    return new ResponseDto<>("success", officeLeaseRate);
  }

  @Operation(summary = "오피스 총 임대 현황 조회", description = "모든 오피스의 임대 현황을 조회할 수 있다.")
  @GetMapping("/offices/overall-rental-status")
  public ResponseDto<RentalStatusDto> getOfficeOverallRentalStatus(
      @RequestHeader("Authorization") String jwt) {
    RentalStatusDto officeLeaseRate = officeOwnerService.getOfficeOverallRentalStatus(jwt);

    return new ResponseDto<>("success", officeLeaseRate);
  }
}