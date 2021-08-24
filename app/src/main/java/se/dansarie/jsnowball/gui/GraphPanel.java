package se.dansarie.jsnowball.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.Timer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import se.dansarie.jsnowball.model.SnowballState;

public abstract class GraphPanel<E> extends JPanel implements ListDataListener {
  private SnowballState state;
  private Map<Integer, Node<E>> nodeMap = new HashMap<>();
  private Map<E, Node<E>> memberMap = new HashMap<>();
  private Node<E> draggedNode = null;
  private Set<GraphListener<E>> listeners = new HashSet<>();
  private float temp = 20;
  private Timer timer = new Timer(50, ev -> redrawGraphs());
  private Predicate<E> filter = m -> true;

  public GraphPanel() {
    GPMouseListener gpm = new GPMouseListener();
    addMouseListener(gpm);
    addMouseMotionListener(gpm);
    addAncestorListener(new AncestorListener() {
      @Override
      public void ancestorAdded(AncestorEvent ev) {
        timer.start();
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
    temp = 20;
    getListModel().addListDataListener(this);
  }

  public void setMemberFilter(Predicate<E> filter) {
    this.filter = Objects.requireNonNull(filter);
  }

  protected abstract Color getColor(E member);

  protected abstract ListModel<E> getListModel();

  protected abstract List<E> getEdges(E member);

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

  @Override
  public void paintComponent(Graphics gr) {
    super.paintComponent(gr);
    Graphics2D g2 = (Graphics2D)gr;
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
    }
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

  private void drawGraph(List<Node<E>> graph, float k) {
    for (Node<E> n1 : graph) {
      n1.clearForces();
    }
    for (Node<E> n1 : graph) {
      for (Node<E> n2 : graph) {
        if (n1 == n2) {
          continue;
        }
        updateForce(n1, n2, x -> k * k / x);
      }
      for (Node<E> n2 : n1.getEdges()) {
        updateForce(n1, n2, x -> -x * x / k);
        updateForce(n2, n1, x -> -x * x / k);
      }
    }

    float minx = Integer.MAX_VALUE;
    float miny = Integer.MAX_VALUE;
    for (Node <E> node : graph) {
      float fabs = (float)Math.hypot(node.force_x, node.force_y);
      float dx = node.force_x;
      float dy = node.force_y;
      if (fabs > temp) {
         dx *= temp / fabs;
         dy *= temp / fabs;
      }
      float x = node.getX() + dx;
      float y = node.getY() + dy;
      minx = Math.min(x, minx);
      miny = Math.min(y, miny);
      if (node != draggedNode) {
        node.setX(x);
        node.setY(y);
      }
    }
    if (minx < 0 || miny < 0) {
      for (Node <E> node : graph) {
        if (minx < 0) {
          node.setX(node.getX() - minx);
        }
        if (miny < 0) {
          node.setY(node.getY() - miny);
        }
      }
    }
  }

  private Rectangle getGraphBounds(List<Node<E>> graph) {
    Rectangle bounds = graph.get(0).getShape().getBounds();
    for (Node<E> node : graph) {
      bounds = bounds.union(node.getShape().getBounds());
    }
    return bounds;
  }

  private void redrawGraphs() {
    List<List<Node<E>>> graphs = getGraphs();
    Collections.sort(graphs, (l1, l2) -> l2.size() - l1.size());

    for (List<Node<E>> graph : graphs) {
      drawGraph(graph, 60);
    }
    if (temp > 0.2) {
      temp *= 0.99;
    }

    for (List<Node<E>> graph : graphs) {
      Rectangle bounds = getGraphBounds(graph);
      for (Node<E> node : graph) {
        if (node != draggedNode) {
          node.setX(node.getX() - bounds.x);
          node.setY(node.getY() - bounds.y);
        }
      }
    }

    Rectangle bounds = null;
    for (List<Node<E>> graph : graphs) {
      Rectangle graphBounds = getGraphBounds(graph);
      /* Check if graph intersects with previous graphs. */
      if (bounds != null && graphBounds.intersects(bounds)) {
        float dx = 0;
        float dy = 0;
        if (bounds.x + bounds.width + graphBounds.width <
            2 * (bounds.y + bounds.height + graphBounds.height)) {
          dx = bounds.x + bounds.width  - graphBounds.x;
        } else {
          dy = bounds.y + bounds.height - graphBounds.y;
        }
        for (Node<E> node : graph) {
          if (node != draggedNode) {
            node.setX(node.getX() + dx);
            node.setY(node.getY() + dy);
          }
        }
        graphBounds = getGraphBounds(graph);
      }

      /* Update bounds of previous graphs with current. */
      if (bounds == null) {
        bounds = graphBounds;
      } else {
        bounds = bounds.union(graphBounds);
      }
    }
    updateSize();
    repaint();
  }

  private List<List<Node<E>>> getGraphs() {
    for (Node<E> no : nodeMap.values()) {
      no.graphnum = -1;
    }
    int nextnum = 0;
    for (Node<E> no : nodeMap.values()) {
      if (no.graphnum != -1) {
        continue;
      }
      no.setGraphnum(nextnum++);
    }

    ArrayList<List<Node<E>>> graphs = new ArrayList<>();
    for (int i = 0; i < nextnum; i++) {
      graphs.add(new ArrayList<>());
    }
    for (Node<E> no : nodeMap.values()) {
      graphs.get(no.graphnum).add(no);
    }

    return graphs;
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
      redrawGraphs();
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
    private Ellipse2D.Float shape = new Ellipse2D.Float(0, 0, 25, 25);
    private int graphnum = -1;
    float force_x = 0;
    float force_y = 0;
    float remain_x = 0;
    float remain_y = 0;

    private Node(E member) {
      this.member = Objects.requireNonNull(member);
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
      force_x = force_y = 0;
    }

    private float getX() {
      return shape.x;
    }

    private float getY() {
      return shape.y;
    }

    private void setX(int x) {
      shape.x = x;
    }

    private void setY(int y) {
      shape.y = y;
    }

    private void setX(float x) {
      x -= remain_x;
      shape.x = (int)x;
      remain_x = shape.x - x;
    }

    private void setY(float y) {
      y -= remain_y;
      shape.y = (int)y;
      remain_y = shape.y - y;
    }

    private void setGraphnum(int num) {
      graphnum = num;
      for (Node<E> no : edges) {
        if (no.graphnum == -1) {
          no.setGraphnum(num);
        } else if (no.graphnum != num) {
          throw new IllegalStateException();
        }
      }
      for (Node<E> no : edgesTo) {
        if (no.graphnum == -1) {
          no.setGraphnum(num);
        } else if (no.graphnum != num) {
          throw new IllegalStateException();
        }
      }
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
