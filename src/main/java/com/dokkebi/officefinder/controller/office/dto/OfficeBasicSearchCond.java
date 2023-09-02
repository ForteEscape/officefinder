package com.dokkebi.officefinder.controller.office.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OfficeBasicSearchCond {

  String legion;
  String city;
  String town;
  String village;
  Integer maxCapacity;
}