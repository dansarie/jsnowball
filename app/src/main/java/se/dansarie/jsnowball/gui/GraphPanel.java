/* Copyright (c) 2021 Marcus Dansarie

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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import se.dansarie.jsnowball.model.Article;
import se.dansarie.jsnowball.model.Author;
import se.dansarie.jsnowball.model.SnowballState;

public abstract class GraphPanel<E> extends JPanel implements ListDataListener {
  private SnowballState state;
  private Map<Integer, Node<E>> nodeMap = new HashMap<>();
  private Map<E, Node<E>> memberMap = new HashMap<>();
  private Node<E> draggedNode = null;
  private Set<GraphListener<E>> listeners = new HashSet<>();
  private Timer timer = new Timer(50, ev -> redrawGraph());
  private Predicate<E> filter = m -> true;

  /* ForceAtlas2 settings. */
  private float kr = 20;
  private float kg = 1;
  private float tau = 1;
  private boolean linlog = false;
  private boolean stronggravity = false;
  private boolean dissuadehubs = false;
  private boolean preventoverlap = false;

  private Action linlogAction = new AbstractAction("LinLog") {
    {
      putValue(Action.SELECTED_KEY, false);
    }
    @Override
    public void actionPerformed(ActionEvent ev) {
      linlog = (Boolean)getValue(Action.SELECTED_KEY);
    }
  };

  private Action gravityAction = new AbstractAction("Strong gravity") {
    {
      putValue(Action.SELECTED_KEY, false);
    }
    @Override
    public void actionPerformed(ActionEvent ev) {
      stronggravity = (Boolean)getValue(Action.SELECTED_KEY);
    }
  };

  private Action dissuadeHubsAction = new AbstractAction("Dissuade hubs") {
    {
      putValue(Action.SELECTED_KEY, false);
    }
    @Override
    public void actionPerformed(ActionEvent ev) {
      dissuadehubs = (Boolean)getValue(Action.SELECTED_KEY);
    }
  };

  private Action preventOverlapAction = new AbstractAction("Prevent overlap") {
    {
      putValue(Action.SELECTED_KEY, false);
    }
    @Override
    public void actionPerformed(ActionEvent ev) {
      preventoverlap = (Boolean)getValue(Action.SELECTED_KEY);
    }
  };

  private Action pauseAction = new AbstractAction("Pause") {
    {
      putValue(Action.SELECTED_KEY, false);
    }
    @Override
    public void actionPerformed(ActionEvent ev) {
      if ((Boolean)getValue(Action.SELECTED_KEY)) {
        timer.stop();
      } else {
        timer.start();
      }
    }
  };

  public GraphPanel() {
    GPMouseListener gpm = new GPMouseListener();
    addMouseListener(gpm);
    addMouseMotionListener(gpm);
    addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorAdded(AncestorEvent ev) {
        if (!((Boolean)pauseAction.getValue(Action.SELECTED_KEY))) {
          timer.start();
        }
      }

      @Override
      public void ancestorMoved(AncestorEvent ev) {
      }

      @Override
      public void ancestorRemoved(AncestorEvent ev) {
        timer.stop();
      }
    });
  }

  public void addGraphListener(GraphListener<E> listener) {
    listeners.add(Objects.requireNonNull(listener));
  }

  public void removeGraphListener(GraphListener<E> listener) {
    listeners.remove(listener);
  }

  protected SnowballState getState() {
    return state;
  }

  public void setState(SnowballState state) {
    if (getListModel() != null) {
      getListModel().removeListDataListener(this);
    }
    this.state = Objects.requireNonNull(state);
    nodeMap.clear();
    memberMap.clear();
    draggedNode = null;
    getListModel().addListDataListener(this);
  }

  public void setMemberFilter(Predicate<E> filter) {
    this.filter = Objects.requireNonNull(filter);
  }

  protected abstract Color getColor(E member);

  protected abstract ListModel<E> getListModel();

  protected abstract List<E> getEdges(E member);

  protected abstract boolean isSelected(E member);

  public Action getLinlogAction() {
    return linlogAction;
  }

  public Action getGravityAction() {
    return gravityAction;
  }

  public Action getDissuadeHubsAction() {
    return dissuadeHubsAction;
  }

  public Action getPreventOverlapAction() {
    return preventOverlapAction;
  }

  public Action getPauseAction() {
    return pauseAction;
  }

  public void setKr(float kr) {
    if (kr > 0) {
      this.kr = kr;
    }
  }

  public void setKg(float kg) {
    if (kg > 0) {
      this.kg = kg;
    }
  }

  public void setTau(float tau) {
    if (tau > 0) {
      this.tau = tau;
    }
  }

  private void updateEdges() {
    for (Node<E> no : nodeMap.values()) {
      no.clearEdges();
    }
    for (Node<E> no : new ArrayList<>(nodeMap.values())) {
      for (E adj : new ArrayList<>(getEdges(no.getMember()))) {
        Node<E> adjNode = memberMap.get(adj);
        if (adjNode == null) {
          continue;
        }
        no.addEdge(adjNode);
        adjNode.addEdgeTo(no);
      }
    }
  }

  private void updateNodes() {
    ListModel<E> listModel = getListModel();
    nodeMap.clear();
    for (int i = 0; i < listModel.getSize(); i++) {
      E member = listModel.getElementAt(i);
      if (!filter.test(member)) {
        continue;
      }
      Node<E> node = memberMap.get(member);
      if (node == null) {
        node = new Node<>(member);
        node.setX(i % 10 * 30);
        node.setY(i / 10 * 30);
        memberMap.put(member, node);
      }
      nodeMap.put(i, node);
    }
    memberMap.values().retainAll(nodeMap.values());
    updateEdges();
    updateSize();
    repaint();
  }

  @Override
  public void contentsChanged(ListDataEvent ev) {
    updateNodes();
  }

  @Override
  public void intervalAdded(ListDataEvent ev) {
    updateNodes();
  }

  @Override
  public void intervalRemoved(ListDataEvent ev) {
    updateNodes();
  }

  private void drawGraph(Graphics2D g2) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    for (Node<E> no : nodeMap.values()) {
      Rectangle bounds1 = no.getShape().getBounds();
      g2.setColor(Color.BLACK);
      int x1 = (int)bounds1.getCenterX();
      int y1 = (int)bounds1.getCenterY();
      for (Node<E> edge : no.getEdges()) {
        Rectangle bounds2 = edge.getShape().getBounds();
        g2.drawLine(x1, y1, (int)bounds2.getCenterX(), (int)bounds2.getCenterY());
      }
    }
    for (Node<E> no : nodeMap.values()) {
      Shape sh = no.getShape();
      g2.setColor(getColor(no.getMember()));
      g2.fill(sh);
      String label = null;
      E member = no.getMember();
      if (member instanceof Article) {
        label = ((Article)member).getLabel();
      } else if (member instanceof Author) {
        label = ((Author)member).getLabel();
      }
      if (label != null) {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        g2.setFont(font);
        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = font.layoutGlyphVector(frc, label.toCharArray(), 0, label.length(),
            Font.LAYOUT_LEFT_TO_RIGHT);
        Rectangle pixelBounds = gv.getPixelBounds(frc, 0, 0);
        Rectangle2D stringBounds = font.getStringBounds(label, frc);
        Rectangle nodeBounds = no.getShape().getBounds();
        g2.setColor(Color.BLACK);
        int x = (int)(nodeBounds.getCenterX() - stringBounds.getWidth()  / 2);
        int y = (int)(nodeBounds.getCenterY() + pixelBounds.getHeight() / 2);
        g2.drawString(label, x, y);
      }
      if (isSelected(no.getMember())) {
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(3));
        g2.draw(sh);
      }
    }
  }

  public void getSVG(Writer writer) throws IOException {
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
    String svgNS = "http://www.w3.org/2000/svg";
    Document document = domImpl.createDocument(svgNS, "svg", null);
    SVGGraphics2D g2 = new SVGGraphics2D(document);
    drawGraph(g2);
    g2.stream(writer);
  }

  @Override
  protected void paintComponent(Graphics gr) {
    super.paintComponent(gr);
    Dimension dim = getSize();
    BufferedImage bi = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(),
        BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g2 = bi.createGraphics();
    drawGraph(g2);
    gr.drawImage(bi, 0, 0, null);
  }

  private void updateSize() {
    Dimension dim = new Dimension();
    for (Node<E> node : nodeMap.values()) {
      Rectangle bounds = node.getShape().getBounds();
      if (bounds.x + bounds.width > dim.width) {
        dim.width = bounds.x + bounds.width;
      }
      if (bounds.y + bounds.height > dim.height) {
        dim.height = bounds.y + bounds.height;
      }
    }
    setSize(dim);
    setPreferredSize(dim);
  }

  private void updateForce(Node<E> n1, Node<E> n2, Function<Float, Float> forceFun) {
    float dx = n1.getX() - n2.getX();
    float dy = n1.getY() - n2.getY();
    float dabs = (float)Math.hypot(dx, dy);
    if (dabs == 0.0) {
      return;
    }
    float force = forceFun.apply(dabs);
    dx *= force / dabs;
    dy *= force / dabs;
    n1.addForce(dx, dy);
  }

  /* @see Jacomy, M., Venturini, T., Heymann, S., & Bastian, M. (2014). ForceAtlas2, a Continuous
     Graph Layout Algorithm for Handy Network Visualization Designed for the Gephi Software. PLoS
     ONE, 9(6), e98679. https://doi.org/10.1371/journal.pone.0098679 */
  private void drawGraph(Collection<Node<E>> graph) {
    for (Node<E> n1 : graph) {
      n1.clearForces();
    }
    float centerx = 0;
    float centery = 0;

    /* Repulsive force. */
    for (Node<E> n1 : graph) {
      for (Node<E> n2 : graph) {
        if (n1 == n2) {
          continue;
        }
        Function<Float, Float> repulsiveForce = d -> {
          if (preventoverlap) {
            d -= n1.getShapeSize() / 2 + n2.getShapeSize() / 2;
          }
          if (d > 0) {
            return kr * (n1.getDegree() + 1) * (n2.getDegree() + 1) / d;
          } else if (d < 0) {
            return kr * (n1.getDegree() + 1) * (n2.getDegree() + 1);
          } else {
            return 0.0F;
          }
        };
        updateForce(n1, n2, repulsiveForce);
      }

      /* Attractive force. */
      for (Node<E> n2 : n1.getEdges()) {
        Function<Float, Float> attractiveForce = d -> {
          if (preventoverlap) {
            d -= n1.getShapeSize() / 2 + n2.getShapeSize() / 2;
          }

          if (d >= 0) {
            float force = -d;
            if (linlog) {
              force = -(float)Math.log(1 + d);
            }
            if (dissuadehubs) {
              force /= 1 + n1.getDegree(); /* FIXME. */
            }
            return force;
          } else { /* Overlap */
            return 0.0F;
          }
        };
        updateForce(n1, n2, attractiveForce);
        updateForce(n2, n1, attractiveForce);
      }
      centerx += n1.getX();
      centery += n1.getY();
    }

    float globalSwing = 0;
    float globalTraction = 0;

    Node<E> virtualCenter = new Node<>(null);
    virtualCenter.setX(centerx / graph.size());
    virtualCenter.setY(centery / graph.size());
    for (Node<E> n1 : graph) {
      float num = -kg * (n1.getDegree() + 1);
      if (stronggravity) {
        updateForce(n1, virtualCenter, x -> num * x);
      } else {
        updateForce(n1, virtualCenter, x -> num);
      }
      globalSwing += (n1.getDegree() + 1) * n1.getSwing();
      globalTraction += (n1.getDegree() + 1) * n1.getTraction();
    }
    float globalSpeed = tau * globalTraction / globalSwing;

    float minx = Integer.MAX_VALUE;
    float miny = Integer.MAX_VALUE;
    for (Node <E> node : graph) {
      float localspeed = 0.1F * globalSpeed / (1 + globalSpeed * (float)Math.sqrt(node.getSwing()));
      float dx = node.force_x * localspeed;
      float dy = node.force_y * localspeed;
      float x = node.getX() + dx;
      float y = node.getY() + dy;
      minx = Math.min(x, minx);
      miny = Math.min(y, miny);
      if (node != draggedNode) {
        node.setX(x);
        node.setY(y);
      }
    }
    for (Node <E> node : graph) {
      node.setX(node.getX() - minx);
      node.setY(node.getY() - miny);
    }
  }

  private void redrawGraph() {
    drawGraph(nodeMap.values());

    updateSize();
    repaint();
  }

  private class GPMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent ev) {
      for (Node<E> no : memberMap.values()) {
        if (no.getShape().contains(ev.getX(), ev.getY())) {
          for (GraphListener<E> gl : listeners) {
            gl.memberSelected(no.getMember());
          }
          return;
        }
      }
      redrawGraph();
    }

    @Override
    public void mouseDragged(MouseEvent ev) {
      if (draggedNode == null) {
        return;
      }
      draggedNode.setX(ev.getX());
      draggedNode.setY(ev.getY());
      updateSize();
      repaint();
    }

    @Override
    public void mousePressed(MouseEvent ev) {
      for (Node<E> no : memberMap.values()) {
        if (no.getShape().contains(ev.getX(), ev.getY())) {
          draggedNode = no;
          break;
        }
      }
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
      draggedNode = null;
    }
  }

  private static class Node<E> {
    private E member;
    private List<Node<E>> edges = new ArrayList<>();
    private List<Node<E>> edgesTo = new ArrayList<>();
    private RectangularShape shape = new Ellipse2D.Float(0, 0, 25, 25);
    float force_x = 0;
    float force_y = 0;
    float previous_force = 0;

    private Node(E member) {
      this.member = member;
    }

    private E getMember() {
      return member;
    }

    private List<Node<E>> getEdges() {
      return Collections.unmodifiableList(new ArrayList<>(edges));
    }

    private Shape getShape() {
      return shape;
    }

    private float getShapeSize() {
      Rectangle2D bounds = shape.getBounds2D();
      return (float)Math.max(bounds.getHeight(), bounds.getWidth());
    }

    private void clearEdges() {
      edges.clear();
      edgesTo.clear();
    }

    private void addEdge(Node<E> edge) {
      if (!edges.contains(Objects.requireNonNull(edge))) {
        edges.add(edge);
      }
    }

    private void addEdgeTo(Node<E> edge) {
      if (!edgesTo.contains(Objects.requireNonNull(edge))) {
        edgesTo.add(edge);
      }
    }

    private void addForce(float x, float y) {
      force_x += x;
      force_y += y;
    }

    private void clearForces() {
      previous_force = (float)Math.hypot(force_x, force_y);
      force_x = force_y = 0;
    }

    private int getDegree() {
      return edgesTo.size();
    }

    private float getSwing() {
      return (float)Math.abs(Math.hypot(force_x, force_y) - previous_force);
    }

    private float getTraction() {
      return ((float)Math.hypot(force_x, force_y) + previous_force) / 2;
    }

    private float getX() {
      return (float)shape.getX();
    }

    private float getY() {
      return (float)shape.getY();
    }

    private void setX(int x) {
      shape.setFrame(x, shape.getY(), shape.getWidth(), shape.getHeight());
    }

    private void setY(int y) {
      shape.setFrame(shape.getX(), y, shape.getWidth(), shape.getHeight());
    }

    private void setX(float x) {
      shape.setFrame(x, shape.getY(), shape.getWidth(), shape.getHeight());
    }

    private void setY(float y) {
      shape.setFrame(shape.getX(), y, shape.getWidth(), shape.getHeight());
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("Node[member: [");
      sb.append(member.toString());
      sb.append("] #edges: ");
      sb.append(edges.size());
      sb.append("]");
      return sb.toString();
    }
  }
}
