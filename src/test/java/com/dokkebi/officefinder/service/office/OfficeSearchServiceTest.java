package com.dokkebi.officefinder.service.office;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.dokkebi.officefinder.controller.office.dto.OfficeAddress;
import com.dokkebi.officefinder.controller.office.dto.OfficeCreateRequestDto;
import com.dokkebi.officefinder.controller.office.dto.OfficeSearchCond;
import com.dokkebi.officefinder.controller.office.dto.OfficeOption;
import com.dokkebi.officefinder.entity.OfficeOwner;
import com.dokkebi.officefinder.entity.office.Office;
import com.dokkebi.officefinder.entity.office.OfficeLocation;
import com.dokkebi.officefinder.repository.OfficeOwnerRepository;
import com.dokkebi.officefinder.repository.office.OfficeRepository;
import com.dokkebi.officefinder.repository.office.condition.OfficeConditionRepository;
import com.dokkebi.officefinder.repository.office.location.OfficeLocationRepository;
import com.dokkebi.officefinder.repository.office.picture.OfficePictureRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class OfficeSearchServiceTest {

  @Autowired
  private OfficeService officeService;

  @Autowired
  private OfficeRepository officeRepository;
  @Autowired
  private OfficeLocationRepository officeLocationRepository;
  @Autowired
  private OfficeConditionRepository officeConditionRepository;
  @Autowired
  private OfficePictureRepository officePictureRepository;

  @Autowired
  private OfficeSearchService officeQueryService;
  @Autowired
  private OfficeOwnerRepository officeOwnerRepository;

  @AfterEach
  void tearDown() {
    officePictureRepository.deleteAllInBatch();
    officeConditionRepository.deleteAllInBatch();
    officeLocationRepository.deleteAllInBatch();
    officeRepository.deleteAllInBatch();
    officeOwnerRepository.deleteAllInBatch();
  }

  @DisplayName("기본 조건(도 행정구역)으로 오피스를 검색할 수 있다. 검색된 오피스는 페이징 처리가 되어 반환된다.")
  @Test
  public void searchOfficeByBasicConditionTest() {
    // given
    OfficeOwner officeOwner = createOfficeOwner("kim", "owner@test.com", "12345", "123-45", 1000L,
        Set.of("ROLE_OFFICE_OWNER"));

    OfficeOwner savedOfficeOwner = officeOwnerRepository.save(officeOwner);

    addOfficeData(savedOfficeOwner);

    OfficeSearchCond cond = new OfficeSearchCond();
    cond.setLegion("경상남도");

    PageRequest pageRequest = PageRequest.of(0, 5);

    // when
    Page<Office> offices = officeQueryService.searchOfficeByDetailCondition(cond, pageRequest);
    List<Office> content = offices.getContent();

    // then
    assertThat(content).hasSize(4)
        .extracting("name", "leaseFee", "maxCapacity", "officeAddress")
        .containsExactlyInAnyOrder(
            tuple("office1", 500000L, 5, "경상남도 김해시 삼계동 삼계로 223"),
            tuple("office2", 1000000L, 10, "경상남도 김해시 삼계동 삼계로 224"),
            tuple("office4", 1500000L, 10, "경상남도 진영시 가츠동 가츠로 4"),
            tuple("office5", 2000000L, 15, "경상남도 김해시 내외동 내외로 12")
        );

    assertThat(content)
        .extracting(Office::getOfficeLocation)
        .extracting(OfficeLocation::getAddress)
        .extracting("legion", "city", "town", "detail", "zipcode")
        .containsExactlyInAnyOrder(
            tuple("경상남도", "김해시", "삼계동", "", 12345),
            tuple("경상남도", "김해시", "삼계동", "", 12348),
            tuple("경상남도", "진영시", "가츠동", "", 12598),
            tuple("경상남도", "김해시", "내외동", "", 12508)
        );

    assertThat(content)
        .extracting(Office::getOfficeCondition)
        .extracting("airCondition", "heaterCondition", "cafe", "printer", "packageSendService",
            "doorLock", "fax", "publicKitchen", "publicLounge", "privateLocker", "tvProjector",
            "whiteboard", "wifi", "showerBooth", "storage")
        .containsExactlyInAnyOrder(
            tuple(false, false, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, false, true, true, true, true, false, false, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, false, false, true, true, true, true,
                false, true)
        );
  }

  @DisplayName("오피스를 검색할 수 있다. 검색된 오피스는 페이징 처리가 되어 반환된다.")
  @Test
  public void searchOfficeTest() {
    // given
    OfficeOwner officeOwner = createOfficeOwner("kim", "owner@test.com", "12345", "123-45", 1000L,
        Set.of("ROLE_OFFICE_OWNER"));

    OfficeOwner savedOfficeOwner = officeOwnerRepository.save(officeOwner);

    addOfficeData(savedOfficeOwner);

    OfficeSearchCond cond = new OfficeSearchCond();
    PageRequest pageRequest = PageRequest.of(0, 5);

    // when
    Page<Office> offices = officeQueryService.searchOfficeByDetailCondition(cond, pageRequest);
    List<Office> content = offices.getContent();

    // then
    assertThat(content).hasSize(5)
        .extracting("name", "leaseFee", "maxCapacity", "officeAddress")
        .containsExactlyInAnyOrder(
            tuple("office1", 500000L, 5, "경상남도 김해시 삼계동 삼계로 223"),
            tuple("office2", 1000000L, 10, "경상남도 김해시 삼계동 삼계로 224"),
            tuple("office3", 1500000L, 10, "부산광역시 동구 좌천동 좌천로 123"),
            tuple("office4", 1500000L, 10, "경상남도 진영시 가츠동 가츠로 4"),
            tuple("office5", 2000000L, 15, "경상남도 김해시 내외동 내외로 12")
        );

    assertThat(content)
        .extracting(Office::getOfficeLocation)
        .extracting(OfficeLocation::getAddress)
        .extracting("legion", "city", "town", "detail", "zipcode")
        .containsExactlyInAnyOrder(
            tuple("경상남도", "김해시", "삼계동", "", 12345),
            tuple("경상남도", "김해시", "삼계동", "", 12348),
            tuple("부산광역시", "동구", "좌천동", "", 12398),
            tuple("경상남도", "진영시", "가츠동", "", 12598),
            tuple("경상남도", "김해시", "내외동", "", 12508)
        );

    assertThat(content)
        .extracting(Office::getOfficeCondition)
        .extracting("airCondition", "heaterCondition", "cafe", "printer", "packageSendService",
            "doorLock", "fax", "publicKitchen", "publicLounge", "privateLocker", "tvProjector",
            "whiteboard", "wifi", "showerBooth", "storage")
        .containsExactlyInAnyOrder(
            tuple(false, false, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, false, true, true, true, true, false, false, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, false, false, true, true, true, true,
                false, true)
        );
  }

  @DisplayName("기본 조건(시, 도 행정구역)으로 오피스를 검색할 수 있다. 검색된 오피스는 페이징 처리가 되어 반환된다.")
  @Test
  public void searchOfficeByBasicConditionTest2() {
    // given
    OfficeOwner officeOwner = createOfficeOwner("kim", "owner@test.com", "12345", "123-45", 1000L,
        Set.of("ROLE_OFFICE_OWNER"));

    OfficeOwner savedOfficeOwner = officeOwnerRepository.save(officeOwner);

    addOfficeData(savedOfficeOwner);

    OfficeSearchCond cond = new OfficeSearchCond();
    cond.setLegion("경상남도");
    cond.setCity("김해시");

    PageRequest pageRequest = PageRequest.of(0, 5);

    // when
    Page<Office> offices = officeQueryService.searchOfficeByDetailCondition(cond, pageRequest);
    List<Office> content = offices.getContent();

    // then
    assertThat(content).hasSize(3)
        .extracting("name", "leaseFee", "maxCapacity", "officeAddress")
        .containsExactlyInAnyOrder(
            tuple("office1", 500000L, 5, "경상남도 김해시 삼계동 삼계로 223"),
            tuple("office2", 1000000L, 10, "경상남도 김해시 삼계동 삼계로 224"),
            tuple("office5", 2000000L, 15, "경상남도 김해시 내외동 내외로 12")
        );

    assertThat(content)
        .extracting(Office::getOfficeLocation)
        .extracting(OfficeLocation::getAddress)
        .extracting("legion", "city", "town", "detail", "zipcode")
        .containsExactlyInAnyOrder(
            tuple("경상남도", "김해시", "삼계동", "", 12345),
            tuple("경상남도", "김해시", "삼계동", "", 12348),
            tuple("경상남도", "김해시", "내외동", "", 12508)
        );

    assertThat(content)
        .extracting(Office::getOfficeCondition)
        .extracting("airCondition", "heaterCondition", "cafe", "printer", "packageSendService",
            "doorLock", "fax", "publicKitchen", "publicLounge", "privateLocker", "tvProjector",
            "whiteboard", "wifi", "showerBooth", "storage")
        .containsExactlyInAnyOrder(
            tuple(false, false, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, false, true, true, true, true, false, false, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, false, false, true, true, true, true,
                false, true)
        );
  }

  @DisplayName("기본 조건(시, 도 행정구역, 최대 수용 인원 수)으로 오피스를 검색할 수 있다. 검색된 오피스는 페이징 처리가 되어 반환된다.")
  @Test
  public void searchOfficeByBasicConditionTest3() {
    // given
    OfficeOwner officeOwner = createOfficeOwner("kim", "owner@test.com", "12345", "123-45", 1000L,
        Set.of("ROLE_OFFICE_OWNER"));

    OfficeOwner savedOfficeOwner = officeOwnerRepository.save(officeOwner);

    addOfficeData(savedOfficeOwner);

    OfficeSearchCond cond = new OfficeSearchCond();
    cond.setLegion("경상남도");
    cond.setCity("김해시");
    cond.setMaxCapacity(5);

    PageRequest pageRequest = PageRequest.of(0, 5);

    // when
    Page<Office> offices = officeQueryService.searchOfficeByDetailCondition(cond, pageRequest);
    List<Office> content = offices.getContent();

    // then
    assertThat(content).hasSize(1)
        .extracting("name", "leaseFee", "maxCapacity", "officeAddress")
        .containsExactlyInAnyOrder(
            tuple("office1", 500000L, 5, "경상남도 김해시 삼계동 삼계로 223")
        );

    assertThat(content)
        .extracting(Office::getOfficeLocation)
        .extracting(OfficeLocation::getAddress)
        .extracting("legion", "city", "town", "detail", "zipcode")
        .containsExactlyInAnyOrder(
            tuple("경상남도", "김해시", "삼계동", "", 12345)
        );

    assertThat(content)
        .extracting(Office::getOfficeCondition)
        .extracting("airCondition", "heaterCondition", "cafe", "printer", "packageSendService",
            "doorLock", "fax", "publicKitchen", "publicLounge", "privateLocker", "tvProjector",
            "whiteboard", "wifi", "showerBooth", "storage")
        .containsExactlyInAnyOrder(
            tuple(false, false, true, true, true, true, true, true, true, true, true, true, true,
                true, true)
        );
  }

  @DisplayName("상세 조건(보유시설, 최대 수용 인원)으로 오피스를 검색할 수 있다. 검색된 오피스는 페이징 처리가 되어 반환된다.")
  @Test
  public void searchOfficeByDetailConditionTest() {
    // given
    OfficeOwner officeOwner = createOfficeOwner("kim", "owner@test.com", "12345", "123-45", 1000L,
        Set.of("ROLE_OFFICE_OWNER"));

    OfficeOwner savedOfficeOwner = officeOwnerRepository.save(officeOwner);

    addOfficeData(savedOfficeOwner);

    OfficeSearchCond cond = new OfficeSearchCond();
    cond.setHaveWhiteBoard(true);
    cond.setHavePublicKitchen(true);
    cond.setMaxCapacity(10);

    PageRequest pageRequest = PageRequest.of(0, 5);

    // when
    Page<Office> offices = officeQueryService.searchOfficeByDetailCondition(cond, pageRequest);
    List<Office> content = offices.getContent();

    // then
    assertThat(content).hasSize(3)
        .extracting("name", "leaseFee", "maxCapacity", "officeAddress")
        .containsExactlyInAnyOrder(
            tuple("office1", 500000L, 5, "경상남도 김해시 삼계동 삼계로 223"),
            tuple("office3", 1500000L, 10, "부산광역시 동구 좌천동 좌천로 123"),
            tuple("office4", 1500000L, 10, "경상남도 진영시 가츠동 가츠로 4")
        );

    assertThat(content)
        .extracting(Office::getOfficeLocation)
        .extracting(OfficeLocation::getAddress)
        .extracting("legion", "city", "town", "detail", "zipcode")
        .containsExactlyInAnyOrder(
            tuple("경상남도", "김해시", "삼계동", "", 12345),
            tuple("부산광역시", "동구", "좌천동", "", 12398),
            tuple("경상남도", "진영시", "가츠동", "", 12598)
        );

    assertThat(content)
        .extracting(Office::getOfficeCondition)
        .extracting("airCondition", "heaterCondition", "cafe", "printer", "packageSendService",
            "doorLock", "fax", "publicKitchen", "publicLounge", "privateLocker", "tvProjector",
            "whiteboard", "wifi", "showerBooth", "storage")
        .containsExactlyInAnyOrder(
            tuple(false, false, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, true, true, true, true, true, true,
                true, true)
        );
  }

  @DisplayName("상세 조건(도 행정구역, 보유시설)으로 오피스를 검색할 수 있다. 검색된 오피스는 페이징 처리가 되어 반환된다.")
  @Test
  public void searchOfficeByDetailConditionTest2() {
    // given
    OfficeOwner officeOwner = createOfficeOwner("kim", "owner@test.com", "12345", "123-45", 1000L,
        Set.of("ROLE_OFFICE_OWNER"));

    OfficeOwner savedOfficeOwner = officeOwnerRepository.save(officeOwner);

    addOfficeData(savedOfficeOwner);

    OfficeSearchCond cond = new OfficeSearchCond();
    cond.setLegion("경상남도");
    cond.setHaveWhiteBoard(true);
    cond.setHavePublicKitchen(true);

    PageRequest pageRequest = PageRequest.of(0, 5);

    // when
    Page<Office> offices = officeQueryService.searchOfficeByDetailCondition(cond, pageRequest);
    List<Office> content = offices.getContent();

    // then
    assertThat(content).hasSize(2)
        .extracting("name", "leaseFee", "maxCapacity", "officeAddress")
        .containsExactlyInAnyOrder(
            tuple("office1", 500000L, 5, "경상남도 김해시 삼계동 삼계로 223"),
            tuple("office4", 1500000L, 10, "경상남도 진영시 가츠동 가츠로 4")
        );

    assertThat(content)
        .extracting(Office::getOfficeLocation)
        .extracting(OfficeLocation::getAddress)
        .extracting("legion", "city", "town", "detail", "zipcode")
        .containsExactlyInAnyOrder(
            tuple("경상남도", "김해시", "삼계동", "", 12345),
            tuple("경상남도", "진영시", "가츠동", "", 12598)
        );

    assertThat(content)
        .extracting(Office::getOfficeCondition)
        .extracting("airCondition", "heaterCondition", "cafe", "printer", "packageSendService",
            "doorLock", "fax", "publicKitchen", "publicLounge", "privateLocker", "tvProjector",
            "whiteboard", "wifi", "showerBooth", "storage")
        .containsExactlyInAnyOrder(
            tuple(false, false, true, true, true, true, true, true, true, true, true, true, true,
                true, true),
            tuple(true, true, true, true, true, true, true, true, true, true, true, true, true,
                true, true)
        );
  }

  private void addOfficeData(OfficeOwner savedOfficeOwner) {
    OfficeCreateRequestDto request = new OfficeCreateRequestDto();

    setOfficeInfo(request, "office1", 5, 500000, 5);
    request.setAddress(setOfficeLocation("경상남도", "김해시", "삼계동", "", "경상남도 김해시 삼계동 삼계로 223", 12345));
    request.setOfficeOption(setOfficeCondition(false, false, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true));

    officeService.createOfficeInfo(request, new ArrayList<>(), savedOfficeOwner.getEmail());

    OfficeCreateRequestDto request2 = new OfficeCreateRequestDto();
    setOfficeInfo(request2, "office2", 10, 1000000, 10);
    request2.setAddress(setOfficeLocation("경상남도", "김해시", "삼계동", "", "경상남도 김해시 삼계동 삼계로 224", 12348));
    request2.setOfficeOption(setOfficeCondition(true, true, false, true, true, true,
        true, false, false, true, true, true, true, true, true, true));

    officeService.createOfficeInfo(request2, new ArrayList<>(), savedOfficeOwner.getEmail());

    OfficeCreateRequestDto request3 = new OfficeCreateRequestDto();
    setOfficeInfo(request3, "office3", 10, 1500000, 10);
    request3.setAddress(setOfficeLocation("부산광역시", "동구", "좌천동", "", "부산광역시 동구 좌천동 좌천로 123", 12398));
    request3.setOfficeOption(setOfficeCondition(true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true));

    officeService.createOfficeInfo(request3, new ArrayList<>(), savedOfficeOwner.getEmail());

    OfficeCreateRequestDto request4 = new OfficeCreateRequestDto();
    setOfficeInfo(request4, "office4", 10, 1500000, 10);
    request4.setAddress(setOfficeLocation("경상남도", "진영시", "가츠동", "", "경상남도 진영시 가츠동 가츠로 4", 12598));
    request4.setOfficeOption(setOfficeCondition(true, true, true, true, true, true,
        true, true, true, true, true, true, true, true, true, true));

    officeService.createOfficeInfo(request4, new ArrayList<>(), savedOfficeOwner.getEmail());

    OfficeCreateRequestDto request5 = new OfficeCreateRequestDto();
    setOfficeInfo(request5, "office5", 15, 2000000, 10);
    request5.setAddress(setOfficeLocation("경상남도", "김해시", "내외동", "", "경상남도 김해시 내외동 내외로 12", 12508));
    request5.setOfficeOption(setOfficeCondition(true, true, true, true, true, true,
        true, false, false, true, true, true, true, false, true, true));

    officeService.createOfficeInfo(request5, new ArrayList<>(), savedOfficeOwner.getEmail());
  }

  private OfficeOwner createOfficeOwner(String name, String email, String password,
      String businessNumber, long point, Set<String> roles) {

    return OfficeOwner.builder()
        .name(name)
        .email(email)
        .password(password)
        .businessNumber(businessNumber)
        .point(point)
        .roles(roles)
        .build();
  }

  private void setOfficeInfo(OfficeCreateRequestDto request, String officeName, int maxCapacity,
      long leaseFee, int maxRoomCount) {
    request.setOfficeName(officeName);
    request.setMaxCapacity(maxCapacity);
    request.setLeaseFee(leaseFee);
    request.setMaxRoomCount(maxRoomCount);
  }

  private OfficeAddress setOfficeLocation(String legion, String city, String town, String detail,
      String street, int zipcode) {

    return OfficeAddress.builder()
        .legion(legion)
        .city(city)
        .town(town)
        .detail(detail)
        .street(street)
        .zipcode(String.valueOf(zipcode))
        .build();
  }

  private OfficeOption setOfficeCondition(boolean airCondition, boolean heaterCondition,
      boolean cafe,
      boolean printer, boolean packageSendService, boolean doorLock, boolean fax,
      boolean publicKitchen, boolean publicLounge, boolean privateLocker, boolean tvProjector,
      boolean whiteboard, boolean wifi, boolean showerBooth, boolean storage, boolean parkArea) {

    return OfficeOption.builder()
        .haveAirCondition(airCondition)
        .haveHeater(heaterCondition)
        .haveCafe(cafe)
        .havePrinter(printer)
        .packageSendServiceAvailable(packageSendService)
        .haveDoorLock(doorLock)
        .faxServiceAvailable(fax)
        .havePublicKitchen(publicKitchen)
        .havePublicLounge(publicLounge)
        .havePrivateLocker(privateLocker)
        .haveTvProjector(tvProjector)
        .haveWhiteBoard(whiteboard)
        .haveWifi(wifi)
        .haveShowerBooth(showerBooth)
        .haveStorage(storage)
        .haveParkArea(parkArea)
        .build();
  }
}
