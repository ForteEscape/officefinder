package com.dokkebi.officefinder.controller.bookmark.dto;

import com.dokkebi.officefinder.entity.bookmark.Bookmark;
import com.dokkebi.officefinder.entity.office.Office;
import com.dokkebi.officefinder.entity.office.OfficePicture;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BookmarkDto {

  private Long id;
  private Long officeId;
  private String officeName;
  private String officeAddress;
  private String officeImagePath;
  private String officeReviewAmount;
  private String officeReviewRate;

  public static BookmarkDto from(Bookmark bookmark, List<OfficePicture> imagePaths) {
    Office office = bookmark.getOffice();
    String representImagePath;

    if (imagePaths == null || imagePaths.isEmpty()){
      representImagePath = "None";
    } else{
      representImagePath = imagePaths.get(0).getFileName();
    }

    double totalRate;

    if (office.getReviewCount() == 0 || office.getTotalRate() == 0){
      totalRate = 0d;
    } else {
      totalRate = (double)(office.getTotalRate() / office.getReviewCount());
      totalRate = Math.round(totalRate * 100.0) / 100.0;
    }

    return BookmarkDto.builder()
        .id(bookmark.getId())
        .officeId(office.getId())
        .officeName(office.getName())
        .officeAddress(office.getOfficeAddress())
        .officeImagePath(representImagePath)
        .officeReviewAmount(String.valueOf(office.getReviewCount()))
        .officeReviewRate(String.valueOf(totalRate))
        .build();
  }
}