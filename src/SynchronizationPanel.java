import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class SynchronizationPanel extends JPanel {

    private JTabbedPane tabs;

    public SynchronizationPanel() {
        setBackground(MainFrame.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = MainFrame.headerLabel("🔄  Process Synchronization");
        JLabel sub = MainFrame.subLabel("Reader-Writer Problem and Dining Philosophers simulation");
        JPanel hdr = new JPanel(); hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        add(hdr, BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setBackground(MainFrame.BG_PANEL);
        tabs.setForeground(MainFrame.TEXT_WHITE);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
        tabs.addTab("Reader-Writer", new ReaderWriterPanel());
        tabs.addTab("🍽 Dining Philosophers", new DiningPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ─────── Reader-Writer Panel ────────────────────────────────────────────
    static class ReaderWriterPanel extends JPanel {
        private JTextArea logArea;
        private JPanel statePanel;
        private javax.swing.Timer simTimer;
        private int readerCount = 0;
        private boolean writing = false;
        private int numReaders = 5;
        private String[] readerStates;
        private String writerState = "Idle";
        private int tick = 0;
        private Random rand = new Random();
        private JLabel readersLbl, writerLbl, statusLbl;

        ReaderWriterPanel() {
            setBackground(MainFrame.BG_DARK);
            setLayout(new BorderLayout(14, 0));
            setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
            readerStates = new String[numReaders];
            Arrays.fill(readerStates, "Idle");
            add(buildLeft(), BorderLayout.WEST);
            add(buildRight(), BorderLayout.CENTER);
        }

        JPanel buildLeft() {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setPreferredSize(new Dimension(220, 0));

            JPanel ctrl = card("Controls");
            JButton start = MainFrame.styledBtn("▶ Start", MainFrame.ACCENT2);
            start.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            JButton stop  = MainFrame.styledBtn("⏹ Stop", MainFrame.DANGER);
            stop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            JButton reset = MainFrame.styledBtn("↺ Reset", MainFrame.BG_CARD);
            reset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

            start.addActionListener(e -> startSim());
            stop.addActionListener(e -> { if(simTimer!=null) simTimer.stop(); });
            reset.addActionListener(e -> {
                if(simTimer!=null) simTimer.stop();
                readerCount=0; writing=false; tick=0;
                Arrays.fill(readerStates,"Idle"); writerState="Idle";
                logArea.setText(""); statePanel.repaint(); updateLabels();
            });
            ctrl.add(start); ctrl.add(Box.createVerticalStrut(6));
            ctrl.add(stop);  ctrl.add(Box.createVerticalStrut(6));
            ctrl.add(reset);
            p.add(ctrl); p.add(Box.createVerticalStrut(10));

            JPanel statusCard = card("Current Status");
            readersLbl = metricLbl("Active Readers", "0");
            writerLbl  = metricLbl("Writer State", "Idle");
            statusLbl  = metricLbl("Database", "Free");
            statusCard.add(readersLbl.getParent()); statusCard.add(Box.createVerticalStrut(6));
            statusCard.add(writerLbl.getParent());  statusCard.add(Box.createVerticalStrut(6));
            statusCard.add(statusLbl.getParent());
            p.add(statusCard); p.add(Box.createVerticalStrut(10));

            JPanel legend = card("Legend");
            legend.add(legendItem("Idle",    new Color(71,85,105)));
            legend.add(Box.createVerticalStrut(4));
            legend.add(legendItem("Waiting", MainFrame.ACCENT3));
            legend.add(Box.createVerticalStrut(4));
            legend.add(legendItem("Reading", MainFrame.ACCENT2));
            legend.add(Box.createVerticalStrut(4));
            legend.add(legendItem("Writing", MainFrame.DANGER));
            p.add(legend);
            return p;
        }

        JPanel buildRight() {
            JPanel p = new JPanel(new BorderLayout(0,10));
            p.setOpaque(false);

            statePanel = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    drawState((Graphics2D)g);
                }
            };
            statePanel.setBackground(MainFrame.BG_PANEL);
            statePanel.setBorder(BorderFactory.createLineBorder(MainFrame.BG_CARD));
            p.add(statePanel, BorderLayout.CENTER);

            JPanel logCard = card("Event Log");
            logCard.setLayout(new BorderLayout());
            logArea = new JTextArea();
            logArea.setBackground(MainFrame.BG_CARD);
            logArea.setForeground(new Color(167,243,208));
            logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            logArea.setEditable(false);
            logArea.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
            JScrollPane ls = new JScrollPane(logArea);
            ls.setBorder(null);
            ls.setPreferredSize(new Dimension(0, 130));
            logCard.add(ls);
            p.add(logCard, BorderLayout.SOUTH);
            return p;
        }

        void drawState(Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = statePanel.getWidth(), h = statePanel.getHeight();
            g2.setColor(MainFrame.BG_PANEL); g2.fillRect(0,0,w,h);

            int dbX = w/2-55, dbY = h/2-40, dbW = 110, dbH = 80;
            Color dbCol = writing ? MainFrame.DANGER : (readerCount > 0 ? MainFrame.ACCENT2 : new Color(71,85,105));
            // DB glow
            g2.setColor(new Color(dbCol.getRed(), dbCol.getGreen(), dbCol.getBlue(), 30));
            g2.fillRoundRect(dbX-8, dbY-8, dbW+16, dbH+16, 22, 22);
            g2.setColor(dbCol);
            g2.fillRoundRect(dbX, dbY, dbW, dbH, 14, 14);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
            String stateStr = writing ? "WRITING" : (readerCount>0 ? "READING ("+readerCount+")" : "FREE");
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("DATABASE", dbX+(dbW-fm.stringWidth("DATABASE"))/2, dbY+30);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            fm = g2.getFontMetrics();
            g2.setColor(new Color(220,220,220));
            g2.drawString(stateStr, dbX+(dbW-fm.stringWidth(stateStr))/2, dbY+52);

            // Readers around
            int n = numReaders;
            double radius = Math.min(w,h)*0.36;
            for (int i=0;i<n;i++) {
                double angle = 2*Math.PI*i/n - Math.PI/2;
                int rx = (int)(w/2 + radius*Math.cos(angle));
                int ry = (int)(h/2 + radius*Math.sin(angle));
                Color rc;
                if (readerStates[i].equals("Reading")) rc = MainFrame.ACCENT2;
                else if (readerStates[i].equals("Waiting")) rc = MainFrame.ACCENT3;
                else rc = new Color(71,85,105);
                // Line to DB
                if (!readerStates[i].equals("Idle")) {
                    g2.setColor(new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 60));
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4,4}, 0));
                    g2.drawLine(rx, ry, w/2, h/2);
                    g2.setStroke(new BasicStroke(1f));
                }
                // Person body
                drawPerson(g2, rx, ry, rc, "R"+(i+1), readerStates[i]);
            }
            // Writer node
            int wx = w-100, wy = 18;
            Color wc;
            if (writerState.equals("Writing")) wc = MainFrame.DANGER;
            else if (writerState.equals("Waiting")) wc = MainFrame.ACCENT3;
            else wc = new Color(71,85,105);
            g2.setColor(new Color(wc.getRed(), wc.getGreen(), wc.getBlue(), 40));
            g2.fillRoundRect(wx-4, wy-4, 78, 52, 12, 12);
            g2.setColor(wc);
            g2.fillRoundRect(wx, wy, 70, 44, 10, 10);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("Writer", wx+10, wy+20);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.setColor(new Color(220,220,220));
            g2.drawString(writerState, wx+10, wy+36);
        }

        // Draw a little stick person
        void drawPerson(Graphics2D g2, int cx, int cy, Color c, String label, String state) {
            int headR = 14;
            // head glow
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 50));
            g2.fillOval(cx-headR-4, cy-headR-4, (headR+4)*2, (headR+4)*2);
            g2.setColor(c);
            g2.fillOval(cx-headR, cy-headR, headR*2, headR*2);
            // face
            g2.setColor(new Color(255,255,255,180));
            g2.fillOval(cx-5, cy-6, 4, 4);  // left eye
            g2.fillOval(cx+1, cy-6, 4, 4);  // right eye
            if (state.equals("Eating")) {
                g2.drawArc(cx-4, cy-1, 8, 5, 0, -180); // smile
            }
            // label
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, cx - fm.stringWidth(label)/2, cy + headR + 14);
            // state label
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 200));
            fm = g2.getFontMetrics();
            g2.drawString(state, cx - fm.stringWidth(state)/2, cy + headR + 24);
        }

        void startSim() {
            if (simTimer != null && simTimer.isRunning()) return;
            simTimer = new javax.swing.Timer(700, e -> step());
            simTimer.start();
        }

        void step() {
            tick++;
            if (writerState.equals("Writing")) {
                if (rand.nextInt(3) == 0) { writing = false; writerState = "Idle"; log("Writer finished writing"); }
            } else if (writerState.equals("Waiting")) {
                if (readerCount == 0 && !writing) { writing = true; writerState = "Writing"; log("Writer acquired lock → Writing"); }
            } else {
                if (rand.nextInt(5) == 0) {
                    if (readerCount == 0) { writing=true; writerState="Writing"; log("Writer acquired lock → Writing"); }
                    else { writerState = "Waiting"; log("Writer waiting (readers active)"); }
                }
            }
            for (int i=0;i<numReaders;i++) {
                if (readerStates[i].equals("Reading")) {
                    if (rand.nextInt(4) == 0) { readerCount--; readerStates[i]="Idle"; log("R"+(i+1)+" finished reading"); }
                } else if (readerStates[i].equals("Waiting")) {
                    if (!writing) { readerCount++; readerStates[i]="Reading"; log("R"+(i+1)+" acquired → Reading"); }
                } else {
                    if (rand.nextInt(4) == 0) {
                        if (!writing) { readerCount++; readerStates[i]="Reading"; log("R"+(i+1)+" acquired → Reading"); }
                        else { readerStates[i]="Waiting"; log("R"+(i+1)+" waiting (writer active)"); }
                    }
                }
            }
            updateLabels(); statePanel.repaint();
        }

        void log(String msg) { logArea.append("[t="+tick+"] "+msg+"\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
        void updateLabels() {
            readersLbl.setText(String.valueOf(readerCount));
            writerLbl.setText(writerState);
            statusLbl.setText(writing ? "Writing" : (readerCount>0 ? "Reading" : "Free"));
        }

        static JPanel card(String t) {
            JPanel p = MainFrame.roundedCard(MainFrame.BG_PANEL, 10);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setForeground(MainFrame.TEXT_GRAY);
            l.setBorder(BorderFactory.createEmptyBorder(0,0,6,0)); p.add(l);
            return p;
        }
        static JLabel metricLbl(String title, String val) {
            JPanel c = MainFrame.roundedCard(MainFrame.BG_CARD, 6);
            c.setLayout(new FlowLayout(FlowLayout.LEFT,8,4));
            JLabel t = new JLabel(title+":"); t.setFont(new Font("Segoe UI",Font.PLAIN,11)); t.setForeground(MainFrame.TEXT_GRAY);
            JLabel v = new JLabel(val); v.setFont(new Font("Segoe UI",Font.BOLD,12)); v.setForeground(MainFrame.TEXT_WHITE);
            c.add(t); c.add(v); c.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
            return v;
        }
        static JPanel legendItem(String label, Color c) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); p.setOpaque(false);
            JPanel dot = new JPanel() {
                protected void paintComponent(Graphics g) {
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(c); g.fillOval(0,0,12,12);
                }
                public Dimension getPreferredSize(){return new Dimension(12,12);}
            };
            dot.setOpaque(false);
            JLabel lbl = new JLabel(label); lbl.setFont(new Font("Segoe UI",Font.PLAIN,11)); lbl.setForeground(MainFrame.TEXT_GRAY);
            p.add(dot); p.add(lbl); return p;
        }
    }

    // ─────── Dining Philosophers Panel ──────────────────────────────────────
    static class DiningPanel extends JPanel {
        private static final int N = 5;
        private int[] state = new int[N]; // 0=thinking,1=hungry,2=eating
        private boolean[] fork = new boolean[N]; // true=available
        private javax.swing.Timer simTimer;
        private JTextArea logArea;
        private JLabel[] stateLbls = new JLabel[N];
        private JPanel drawPanel;
        private Random rand = new Random();
        private int tick = 0;
        static final Color[] PHILO_COLORS = {
            new Color(99,102,241), new Color(16,185,129), new Color(245,158,11),
            new Color(239,68,68),  new Color(139,92,246)
        };
        static final String[] NAMES  = {"Alice","Bob","Carol","Dave","Eve"};
        static final String[] SNAMES = {"Thinking","Hungry","Eating"};

        DiningPanel() {
            setBackground(MainFrame.BG_DARK);
            setLayout(new BorderLayout(14,0));
            setBorder(BorderFactory.createEmptyBorder(12,0,0,0));
            Arrays.fill(fork, true);
            add(buildLeft(), BorderLayout.WEST);
            add(buildRight(), BorderLayout.CENTER);
        }

        JPanel buildLeft() {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setPreferredSize(new Dimension(210,0));

            JPanel ctrl = card("Controls");
            JButton start = MainFrame.styledBtn("▶ Start", MainFrame.ACCENT2);
            start.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JButton stop  = MainFrame.styledBtn("⏹ Stop",  MainFrame.DANGER);
            stop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JButton reset = MainFrame.styledBtn("↺ Reset", MainFrame.BG_CARD);
            reset.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            start.addActionListener(e -> { if(simTimer==null||!simTimer.isRunning()){ simTimer=new javax.swing.Timer(800,ev->step()); simTimer.start(); }});
            stop.addActionListener(e -> { if(simTimer!=null) simTimer.stop(); });
            reset.addActionListener(e -> {
                if(simTimer!=null) simTimer.stop();
                Arrays.fill(state,0); Arrays.fill(fork,true); tick=0;
                logArea.setText(""); drawPanel.repaint(); updateStateLbls();
            });
            ctrl.add(start); ctrl.add(Box.createVerticalStrut(6));
            ctrl.add(stop);  ctrl.add(Box.createVerticalStrut(6));
            ctrl.add(reset);
            p.add(ctrl); p.add(Box.createVerticalStrut(10));

            JPanel stCard = card("Philosopher States");
            for (int i=0;i<N;i++) {
                JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT,6,3));
                row.setOpaque(false);
                JPanel dot = colorDot(PHILO_COLORS[i]);
                JLabel nm = new JLabel(NAMES[i]); nm.setFont(new Font("Segoe UI",Font.BOLD,12)); nm.setForeground(MainFrame.TEXT_WHITE);
                stateLbls[i] = new JLabel("Thinking"); stateLbls[i].setFont(new Font("Segoe UI",Font.PLAIN,11)); stateLbls[i].setForeground(MainFrame.TEXT_GRAY);
                row.add(dot); row.add(nm); row.add(stateLbls[i]);
                stCard.add(row);
            }
            p.add(stCard); p.add(Box.createVerticalStrut(10));

            JPanel legend = card("Legend");
            legend.add(legendItem("💭 Thinking", new Color(71,85,105)));
            legend.add(Box.createVerticalStrut(3));
            legend.add(legendItem("🍽 Hungry",   MainFrame.ACCENT3));
            legend.add(Box.createVerticalStrut(3));
            legend.add(legendItem("🥢 Eating",   MainFrame.ACCENT2));
            legend.add(Box.createVerticalStrut(6));
            legend.add(legendItem("🥄 Fork: Free",  new Color(200,200,200)));
            legend.add(Box.createVerticalStrut(3));
            legend.add(legendItem("🔴 Fork: Taken",  MainFrame.DANGER));
            p.add(legend);
            return p;
        }

        JPanel buildRight() {
            JPanel p = new JPanel(new BorderLayout(0,10));
            p.setOpaque(false);

            drawPanel = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    drawPhilosophers((Graphics2D)g);
                }
            };
            drawPanel.setBackground(MainFrame.BG_PANEL);
            drawPanel.setBorder(BorderFactory.createLineBorder(MainFrame.BG_CARD));
            p.add(drawPanel, BorderLayout.CENTER);

            JPanel logCard = card("Event Log");
            logCard.setLayout(new BorderLayout());
            logArea = new JTextArea();
            logArea.setBackground(MainFrame.BG_CARD);
            logArea.setForeground(new Color(167,243,208));
            logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
            logArea.setEditable(false);
            logArea.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
            JScrollPane ls = new JScrollPane(logArea);
            ls.setBorder(null);
            ls.setPreferredSize(new Dimension(0,110));
            logCard.add(ls);
            p.add(logCard, BorderLayout.SOUTH);
            return p;
        }

        void drawPhilosophers(Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = drawPanel.getWidth(), h = drawPanel.getHeight();
            g2.setColor(MainFrame.BG_PANEL); g2.fillRect(0,0,w,h);

            int cx = w/2, cy = h/2;
            int tableR = Math.min(w,h)/5;

            // Table wood texture
            g2.setColor(new Color(42,58,80));
            g2.fillOval(cx-tableR-8, cy-tableR-8, (tableR+8)*2, (tableR+8)*2);
            // Table surface
            GradientPaint tablePaint = new GradientPaint(cx-tableR, cy-tableR,
                new Color(55,75,100), cx+tableR, cy+tableR, new Color(35,52,72));
            g2.setPaint(tablePaint);
            g2.fillOval(cx-tableR, cy-tableR, tableR*2, tableR*2);
            g2.setPaint(null);
            // Table edge
            g2.setColor(new Color(71,95,120));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(cx-tableR, cy-tableR, tableR*2, tableR*2);
            g2.setStroke(new BasicStroke(1));
            // Table label
            g2.setColor(new Color(148,163,184,180));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString("DINING", cx - fm.stringWidth("DINING")/2, cy - 4);
            g2.drawString("TABLE", cx - fm.stringWidth("TABLE")/2, cy + 12);

            // Plate ring on table
            g2.setColor(new Color(80,100,125,100));
            for (int i=0;i<N;i++) {
                double ang = 2*Math.PI*i/N - Math.PI/2;
                int px = (int)(cx + (tableR-16)*Math.cos(ang));
                int py = (int)(cy + (tableR-16)*Math.sin(ang));
                g2.fillOval(px-10, py-10, 20, 20);
            }

            double philoR = Math.min(w,h)*0.34;
            double forkR  = (tableR + philoR*0.42);

            // Draw forks/spoons between philosophers
            for (int i = 0; i < N; i++) {
                double forkAngle = 2*Math.PI*i/N - Math.PI/2 + Math.PI/N;
                int fx = (int)(cx + forkR * Math.cos(forkAngle));
                int fy = (int)(cy + forkR * Math.sin(forkAngle));
                drawSpoon(g2, fx, fy, forkAngle + Math.PI/2, fork[i]);
            }

            // Draw philosophers (persons sitting around)
            for (int i = 0; i < N; i++) {
                double angle = 2*Math.PI*i/N - Math.PI/2;
                int px = (int)(cx + philoR * Math.cos(angle));
                int py = (int)(cy + philoR * Math.sin(angle));
                drawPhilosopher(g2, px, py, i, angle);
            }
        }

        // Draw a seated person figure
        void drawPhilosopher(Graphics2D g2, int cx, int cy, int idx, double facingAngle) {
            Color c = PHILO_COLORS[idx];
            int st = state[idx];
            Color bodyCol;
            if (st == 1) bodyCol = MainFrame.ACCENT3;
            else if (st == 2) bodyCol = MainFrame.ACCENT2;
            else bodyCol = c;

            // Chair / seat shadow
            g2.setColor(new Color(0,0,0,60));
            g2.fillOval(cx-20, cy-10, 40, 20);

            // Body (torso)
            g2.setColor(bodyCol.darker());
            g2.fillRoundRect(cx-12, cy-8, 24, 20, 8, 8);
            g2.setColor(bodyCol);
            g2.fillRoundRect(cx-11, cy-9, 22, 18, 8, 8);

            // Head
            int headR = 14;
            // Head shadow
            g2.setColor(new Color(0,0,0,40));
            g2.fillOval(cx-headR+2, cy-headR-18+2, headR*2, headR*2);
            // Head fill
            g2.setColor(new Color(255, 220, 180)); // skin tone
            g2.fillOval(cx-headR, cy-headR-18, headR*2, headR*2);
            // Hair (colored by philosopher)
            g2.setColor(c);
            g2.fillArc(cx-headR, cy-headR-18, headR*2, headR*2, 0, 180);
            g2.fillRect(cx-headR, cy-headR-18, headR*2, 6);

            // Eyes
            g2.setColor(new Color(40,40,60));
            g2.fillOval(cx-7, cy-headR-14, 5, 5);
            g2.fillOval(cx+2, cy-headR-14, 5, 5);
            // Eye shine
            g2.setColor(Color.WHITE);
            g2.fillOval(cx-6, cy-headR-13, 2, 2);
            g2.fillOval(cx+3, cy-headR-13, 2, 2);

            // Mouth / expression
            g2.setColor(new Color(180,100,80));
            if (st == 2) { // eating - big smile
                g2.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cx-5, cy-headR-6, 10, 7, 0, -180);
                g2.setStroke(new BasicStroke(1));
            } else if (st == 1) { // hungry - wavy mouth
                g2.fillRoundRect(cx-4, cy-headR-5, 8, 3, 2, 2);
            } else { // thinking - slight smile
                g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cx-4, cy-headR-6, 8, 5, 0, -120);
                g2.setStroke(new BasicStroke(1));
            }

            // Arms extended toward table
            if (st == 2) { // eating - arms out
                g2.setColor(new Color(255,210,165));
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // arms pointing inward toward table center
                int drawPanelCX = drawPanel.getWidth()/2;
                int drawPanelCY = drawPanel.getHeight()/2;
                double toTable = Math.atan2(drawPanelCY - cy, drawPanelCX - cx);
                int armLen = 20;
                g2.drawLine(cx-6, cy, (int)(cx-6 + armLen*Math.cos(toTable)), (int)(cy + armLen*Math.sin(toTable)));
                g2.drawLine(cx+6, cy, (int)(cx+6 + armLen*Math.cos(toTable)), (int)(cy + armLen*Math.sin(toTable)));
                g2.setStroke(new BasicStroke(1));
            }

            // Thought bubble if thinking
            if (st == 0) {
                g2.setColor(new Color(200,210,230,140));
                int bx = cx + 10, by = cy - headR - 30;
                g2.fillOval(cx+8, cy-headR-20, 5, 5);
                g2.fillOval(cx+11, cy-headR-26, 7, 7);
                g2.fillOval(cx+14, cy-headR-34, 20, 14);
                g2.setColor(new Color(80,100,130));
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 8));
                g2.drawString("💭", cx+15, cy-headR-23);
            }

            // Name tag
            g2.setColor(bodyCol);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            fm = g2.getFontMetrics();
            String name = NAMES[idx];
            int nameW = fm.stringWidth(name);
            // Name background
            g2.setColor(new Color(15,23,42,180));
            g2.fillRoundRect(cx-nameW/2-4, cy+14, nameW+8, 16, 6, 6);
            g2.setColor(bodyCol);
            g2.drawString(name, cx-nameW/2, cy+26);

            // State pill
            String stName = SNAMES[st];
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            fm = g2.getFontMetrics();
            int stW = fm.stringWidth(stName);
            Color stColor;
            if (st == 1) stColor = MainFrame.ACCENT3;
            else if (st == 2) stColor = MainFrame.ACCENT2;
            else stColor = new Color(71,85,105);
            g2.setColor(new Color(stColor.getRed(), stColor.getGreen(), stColor.getBlue(), 180));
            g2.fillRoundRect(cx-stW/2-3, cy+31, stW+6, 13, 6, 6);
            g2.setColor(Color.WHITE);
            g2.drawString(stName, cx-stW/2, cy+41);
        }

        // Draw a spoon/fork between seats
        void drawSpoon(Graphics2D g2, int cx, int cy, double angle, boolean available) {
            Color c = available ? new Color(200,200,210) : MainFrame.DANGER;
            g2.setColor(available ? new Color(0,0,0,40) : new Color(200,0,0,30));
            // shadow
            g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx+1, cy+1,
                (int)(cx + 1 + 14*Math.cos(angle)), (int)(cy + 1 + 14*Math.sin(angle)));
            g2.setStroke(new BasicStroke(1));

            // Handle
            g2.setColor(c);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(cx, cy, (int)(cx + 14*Math.cos(angle)), (int)(cy + 14*Math.sin(angle)));
            g2.setStroke(new BasicStroke(1));

            // Spoon bowl (oval at the end)
            int ex = (int)(cx + 14*Math.cos(angle));
            int ey = (int)(cy + 14*Math.sin(angle));
            // rotate oval
            Graphics2D g3 = (Graphics2D)g2.create();
            g3.translate(ex, ey);
            g3.rotate(angle);
            g3.setColor(c);
            g3.fillOval(-5, -4, 10, 8);
            if (available) {
                g3.setColor(new Color(240,240,255,180));
                g3.fillOval(-3, -2, 6, 4);
            }
            g3.dispose();

            // Fork label F0..F4
            g2.setColor(new Color(148,163,184,160));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            FontMetrics fm = g2.getFontMetrics();
            String fLabel = "F"+(Arrays.asList(fork).indexOf(available) >= 0 ? "" : "");
            // find which fork index this is – we'll just not label (too complex in this method)
        }

        void step() {
            tick++;
            for (int i=0;i<N;i++) {
                if (state[i]==2) {
                    if (rand.nextInt(3)==0) {
                        fork[i]=true; fork[(i+1)%N]=true;
                        state[i]=0; log(NAMES[i]+" finished eating → Thinking 💭");
                    }
                } else if (state[i]==1) {
                    int left=i, right=(i+1)%N;
                    if (fork[left] && fork[right]) {
                        fork[left]=false; fork[right]=false;
                        state[i]=2; log(NAMES[i]+" picked up forks → Eating 🥢");
                    }
                } else {
                    if (rand.nextInt(4)==0) { state[i]=1; log(NAMES[i]+" is Hungry 🍽"); }
                }
            }
            updateStateLbls(); drawPanel.repaint();
        }

        void log(String msg) { logArea.append("[t="+tick+"] "+msg+"\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
        void updateStateLbls() {
            Color[] sc = {new Color(71,85,105), MainFrame.ACCENT3, MainFrame.ACCENT2};
            for (int i=0;i<N;i++) { stateLbls[i].setText(SNAMES[state[i]]); stateLbls[i].setForeground(sc[state[i]]); }
        }

        static JPanel card(String t) { return ReaderWriterPanel.card(t); }
        static JPanel legendItem(String l, Color c) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,6,0)); p.setOpaque(false);
            JPanel dot = new JPanel() {
                protected void paintComponent(Graphics g) {
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(c); g.fillOval(0,0,10,10);
                }
                public Dimension getPreferredSize(){return new Dimension(10,10);}
            };
            dot.setOpaque(false);
            JLabel lbl = new JLabel(l); lbl.setFont(new Font("Segoe UI",Font.PLAIN,11)); lbl.setForeground(MainFrame.TEXT_GRAY);
            p.add(dot); p.add(lbl); return p;
        }
        static JPanel colorDot(Color c) {
            JPanel d = new JPanel() {
                protected void paintComponent(Graphics g) {
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(c); g.fillOval(0,0,12,12);
                }
                public Dimension getPreferredSize(){return new Dimension(12,12);}
            };
            d.setOpaque(false); return d;
        }
        FontMetrics fm;
    }
}
