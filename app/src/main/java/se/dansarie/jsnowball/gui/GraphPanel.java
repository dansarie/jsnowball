package se.dansarie.jsnowball.gui;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import se.dansarie.jsnowball.model.SnowballState;

public abstract class GraphPanel<E> extends JPanel implements ListDataListener {
  private SnowballState state;
  private Map<Integer, Node<E>> nodeMap = new HashMap<>();
  private Map<E, Node<E>> memberMap = new HashMap<>();
  private Node<E> clickedNode = null;

  public GraphPanel() {
    GPMouseListener gpm = new GPMouseListener();
    addMouseListener(gpm);
    addMouseMotionListener(gpm);
  }

  protected SnowballState getState() {
    return state;
  }

  public void setState(SnowballState state) {
    if (getListModel() != null) {
      getListModel().removeListDataListener(this);
    }
    this.state = Objects.requireNonNull(state);
    getListModel().addListDataListener(this);
  }

  protected abstract ListModel<E> getListModel();

  protected abstract List<E> getEdges(E member);

  private void addNode(int idx) {
    E element = getListModel().getElementAt(idx);
    Node<E> no = new Node<>(element);
    no.setX((idx % 10) * 30);
    no.setY((idx / 10) * 30);
    nodeMap.put(idx, no);
    memberMap.put(element, no);
    updateEdges(no);
  }

  private int findMemberIdx(E member) {
    ListModel<E> listModel = getListModel();
    for (int i = 0; i < listModel.getSize(); i++) {
      if (listModel.getElementAt(i) == member) {
        return i;
      }
    }
    return -1;
  }

  private void updateEdges(Node<E> node) {
    ArrayList<Node<E>> edges = new ArrayList<>();
    for (E adj : getEdges(node.getMember())) {
      Node<E> adjNode = memberMap.get(adj);
      if (adjNode == null) {
        addNode(findMemberIdx(adj));
        adjNode = memberMap.get(adj);
      }
      edges.add(adjNode);
    }
    node.setEdges(edges);
  }

  @Override
  public void contentsChanged(ListDataEvent ev) {
    for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
      if (!nodeMap.containsKey(i)) {
        addNode(i);
      }
      updateEdges(nodeMap.get(i));
    }
    repaint();
  }

  @Override
  public void intervalAdded(ListDataEvent ev) {
    for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
      addNode(i);
    }
    repaint();
  }

  @Override
  public void intervalRemoved(ListDataEvent ev) {
    for (int i = ev.getIndex0(); i <= ev.getIndex1(); i++) {
      memberMap.remove(nodeMap.get(i));
      nodeMap.remove(i);
    }
    repaint();
  }

  @Override
  public void paintComponent(Graphics gr) {
    super.paintComponent(gr);
    Graphics2D g2 = (Graphics2D)gr;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    for (Node<E> no : nodeMap.values()) {
      Shape sh = no.getShape();
      g2.fill(sh);
      int x1 = (int)sh.getBounds().getCenterX();
      int y1 = (int)sh.getBounds().getCenterY();
      for (Node<E> edge : no.getEdges()) {
        Rectangle bounds = edge.getShape().getBounds();
        g2.drawLine(x1, y1, (int)bounds.getCenterX(), (int)bounds.getCenterY());
      }
    }
  }

  private class GPMouseListener extends MouseAdapter {
    @Override
    public void mouseDragged(MouseEvent ev) {
      if (clickedNode == null) {
        return;
      }
      clickedNode.setX(ev.getX());
      clickedNode.setY(ev.getY());
      repaint();
    }

    @Override
    public void mousePressed(MouseEvent ev) {
      for (Node<E> no : memberMap.values()) {
        if (no.getShape().contains(ev.getX(), ev.getY())) {
          clickedNode = no;
          System.out.println(clickedNode);
          break;
        }
      }
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
      clickedNode = null;
    }
  }

  private static class Node<E> {
    private E member;
    private List<Node<E>> edges = new ArrayList<>();
    private Ellipse2D.Float shape = new Ellipse2D.Float(0, 0, 25, 25);

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

    private void setEdges(List<Node<E>> edges) {
      this.edges = Objects.requireNonNull(edges);
    }

    private void setX(int x) {
      shape.x = x;
    }

    private void setY(int y) {
      shape.y = y;
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
