/* Copyright (c) 2021-2023 Marcus Dansarie

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program. If not, see <http://www.gnu.org/licenses/>. */

package se.dansarie.jsnowball.gui;

import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

public class RhombusShape extends RectangularShape {
  private Polygon polygon;

  public RhombusShape(int size) {
    setFrame(0, 0, size, size);
  }

  @Override
  public double getHeight() {
    return polygon.getBounds().getHeight();
  }

  @Override
  public double getWidth() {
    return polygon.getBounds().getWidth();
  }

  @Override
  public double getX() {
    return polygon.getBounds().getX();
  }

  @Override
  public double getY() {
    return polygon.getBounds().getY();
  }

  @Override
  public boolean isEmpty() {
    return polygon.getBounds().isEmpty();
  }

  @Override
  public void setFrame(double x, double y, double w, double h) {
    polygon = new Polygon();
    polygon.addPoint((int)x,           (int)(y + h / 2));
    polygon.addPoint((int)(x + w / 2), (int)(y + h));
    polygon.addPoint((int)(x + w),     (int)(y + h / 2));
    polygon.addPoint((int)(x + w / 2), (int)y);
  }

  @Override
  public PathIterator getPathIterator(AffineTransform at) {
    return polygon.getPathIterator(at);
  }

  @Override
  public boolean contains(double x, double y) {
    return polygon.contains(x, y);
  }

  @Override
  public boolean contains(double x, double y, double w, double h) {
    return polygon.contains(x, y, w, h);
  }

  @Override
  public boolean intersects(double x, double y, double w, double h) {
    return polygon.intersects(x, y, w, h);
  }

  @Override
  public Rectangle2D getBounds2D() {
    return polygon.getBounds2D();
  }
}
