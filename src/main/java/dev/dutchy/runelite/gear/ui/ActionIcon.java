package dev.dutchy.runelite.gear.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

enum ActionIcon implements Icon {

    ADD,
    RENAME,
    DELETE,
    EXPANDED,
    COLLAPSED,
    MOVE_UP,
    MOVE_DOWN,
    TRASH,
    BACK,
    CHECK,
    SYNC,
    GRID,
    CLOCK,
    SEARCH,
    PIN,
    KEY,
    MORE,
    COPY,
    HELP;

    private static final int SIZE = 10;
    private static final int INSET = 1;
    private static final float STROKE_WIDTH = 1.6f;

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

    private static Path2D pencil() {
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(1.0, 9.0);
        shape.lineTo(1.7, 6.5);
        shape.lineTo(7.1, 1.1);
        shape.lineTo(8.9, 2.9);
        shape.lineTo(3.5, 8.3);
        shape.closePath();
        return shape;
    }

    private static void trash(Graphics2D g2) {
        g2.draw(new Line2D.Double(1.3, 2.8, 8.7, 2.8));
        g2.draw(new Line2D.Double(3.8, 1.4, 6.2, 1.4));
        Path2D.Double body = new Path2D.Double();
        body.moveTo(2.3, 3.6);
        body.lineTo(3.0, 8.9);
        body.lineTo(7.0, 8.9);
        body.lineTo(7.7, 3.6);
        g2.draw(body);
    }

    private static void arrow(Graphics2D g2, boolean up) {
        double tip = up ? 1.4 : 8.6;
        double tail = up ? 9.0 : 1.0;
        double shoulder = up ? 4.8 : 5.2;
        g2.draw(new Line2D.Double(5.0, tail, 5.0, up ? 3.0 : 7.0));
        Path2D.Double head = new Path2D.Double();
        head.moveTo(2.2, shoulder);
        head.lineTo(5.0, tip);
        head.lineTo(7.8, shoulder);
        head.closePath();
        g2.fill(head);
    }

    private static void clock(Graphics2D g2) {
        g2.draw(new Ellipse2D.Double(1.2, 1.2, 7.6, 7.6));
        g2.draw(new Line2D.Double(5.0, 2.8, 5.0, 5.2));
        g2.draw(new Line2D.Double(5.0, 5.2, 7.0, 6.4));
    }

    private static void search(Graphics2D g2) {
        g2.draw(new Ellipse2D.Double(1.2, 1.2, 5.6, 5.6));
        g2.draw(new Line2D.Double(6.4, 6.4, 9.0, 9.0));
    }

    private static void pin(Graphics2D g2) {
        Path2D.Double head = new Path2D.Double();
        head.moveTo(3.0, 1.2);
        head.lineTo(7.0, 1.2);
        head.lineTo(6.4, 4.4);
        head.lineTo(8.2, 6.2);
        head.lineTo(1.8, 6.2);
        head.lineTo(3.6, 4.4);
        head.closePath();
        g2.fill(head);
        g2.draw(new Line2D.Double(5.0, 6.2, 5.0, 9.2));
    }

    private static void key(Graphics2D g2) {
        g2.draw(new RoundRectangle2D.Double(1.0, 2.5, 8.0, 5.0, 2.0, 2.0));
        g2.draw(new Line2D.Double(3.0, 5.0, 7.0, 5.0));
    }

    private static void more(Graphics2D g2) {
        for (int i = 0; i < 3; i++) {
            g2.fill(new Ellipse2D.Double(0.6 + i * 3.2, 3.8, 2.4, 2.4));
        }
    }

    private static void help(Graphics2D g2) {
        g2.draw(new Arc2D.Double(2.2, 0.8, 5.6, 5.0, 200, -250, Arc2D.OPEN));
        g2.draw(new Line2D.Double(5.0, 5.8, 5.0, 7.0));
        g2.fill(new Ellipse2D.Double(4.2, 8.0, 1.7, 1.7));
    }

    private static void copy(Graphics2D g2) {
        g2.draw(new Rectangle2D.Double(1.2, 3.2, 5.4, 5.6));
        g2.draw(new Rectangle2D.Double(3.4, 1.2, 5.4, 5.6));
    }

    private static void grid(Graphics2D g2) {
        for (int column = 0; column < 2; column++) {
            for (int row = 0; row < 2; row++) {
                g2.fill(new Rectangle2D.Double(1.2 + column * 4.4, 1.2 + row * 4.4, 3.2, 3.2));
            }
        }
    }

    private static void sync(Graphics2D g2) {
        g2.draw(new Arc2D.Double(1.6, 1.6, 6.8, 6.8, 40, 290, Arc2D.OPEN));
        Path2D.Double head = new Path2D.Double();
        head.moveTo(9.4, 0.8);
        head.lineTo(9.6, 4.6);
        head.lineTo(6.2, 3.4);
        head.closePath();
        g2.fill(head);
    }

    private static Path2D chevron(double x1, double y1, double x2, double y2, double x3, double y3) {
        Path2D.Double shape = new Path2D.Double();
        shape.moveTo(x1, y1);
        shape.lineTo(x2, y2);
        shape.lineTo(x3, y3);
        return shape;
    }

    @Override
    public void paintIcon(Component component, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(component.getForeground());
            g2.setStroke(new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.translate(x, y);
            g2.scale(SIZE / 10.0, SIZE / 10.0);
            int far = SIZE - INSET;
            int middle = SIZE / 2;
            switch (this) {
                case ADD:
                    g2.drawLine(middle, INSET, middle, far);
                    g2.drawLine(INSET, middle, far, middle);
                    break;
                case DELETE:
                    g2.drawLine(INSET, INSET, far, far);
                    g2.drawLine(far, INSET, INSET, far);
                    break;
                case RENAME:
                    g2.fill(pencil());
                    break;
                case EXPANDED:
                    g2.draw(chevron(1.5, 3.5, 5.0, 7.0, 8.5, 3.5));
                    break;
                case COLLAPSED:
                    g2.draw(chevron(3.5, 1.5, 7.0, 5.0, 3.5, 8.5));
                    break;
                case MOVE_UP:
                    arrow(g2, true);
                    break;
                case MOVE_DOWN:
                    arrow(g2, false);
                    break;
                case TRASH:
                    trash(g2);
                    break;
                case BACK:
                    g2.draw(chevron(6.5, 1.5, 3.0, 5.0, 6.5, 8.5));
                    break;
                case CHECK:
                    g2.draw(chevron(1.5, 5.2, 4.0, 7.8, 8.5, 2.2));
                    break;
                case SYNC:
                    sync(g2);
                    break;
                case GRID:
                    grid(g2);
                    break;
                case CLOCK:
                    clock(g2);
                    break;
                case SEARCH:
                    search(g2);
                    break;
                case PIN:
                    pin(g2);
                    break;
                case KEY:
                    key(g2);
                    break;
                case MORE:
                    more(g2);
                    break;
                case COPY:
                    copy(g2);
                    break;
                case HELP:
                    help(g2);
                    break;
                default:
                    break;
            }
        } finally {
            g2.dispose();
        }
    }
}
