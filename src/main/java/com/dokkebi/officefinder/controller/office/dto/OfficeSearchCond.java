package com.dokkebi.officefinder.controller.office.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OfficeSearchCond {

  String legion;
  String city;
  String town;
  Integer maxCapacity;
  Boolean haveAirCondition;
  Boolean haveCafe;
  Boolean havePrinter;
  Boolean packageSendServiceAvailable;
  Boolean haveDoorLock;
  Boolean faxServiceAvailable;
  Boolean havePublicKitchen;
  Boolean havePublicLounge;
  Boolean havePrivateLocker;
  Boolean haveTvProjector;
  Boolean haveWhiteBoard;
  Boolean haveWifi;
  Boolean haveShowerBooth;
  Boolean haveStorage;
  Boolean haveHeater;
  Boolean haveParkArea;
}
