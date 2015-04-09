/**
 * Area.java
 * Created: 25.04.2014, 16:03:46
 */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tu_berlin.coga.zet.model;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface Area {

  public AreaType getAreaType();

  public Room getAssociatedRoom();

  public PlanPolygon<PlanEdge> getPolygon();

  public double areaMeter();

}
