import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setBackground(MainFrame.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ── Header ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel hLeft = new JPanel();
        hLeft.setOpaque(false);
        hLeft.setLayout(new BoxLayout(hLeft, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Mini OS Simulator");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(MainFrame.TEXT_WHITE);
        JLabel sub = new JLabel("CSE 3203  \u2022  Operating Systems");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(MainFrame.TEXT_GRAY);
        hLeft.add(title);
        hLeft.add(Box.createVerticalStrut(3));
        hLeft.add(sub);

        // Status badge
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badge.setOpaque(false);
        JPanel dot = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(MainFrame.ACCENT2); g.fillOval(0,0,10,10);
            }
            public Dimension getPreferredSize() { return new Dimension(10,10); }
        };
        dot.setOpaque(false);
        JLabel statusLbl = new JLabel("System Active");
        statusLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLbl.setForeground(MainFrame.ACCENT2);
        badge.add(dot); badge.add(statusLbl);
        hLeft.add(Box.createVerticalStrut(6));
        hLeft.add(badge);

        header.add(hLeft, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Body ────────────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(Box.createVerticalStrut(16));

        // ── Row 1: Stat cards ────────────────────────────────────────────────
        JPanel row1 = new JPanel(new GridLayout(1, 4, 14, 0));
        row1.setOpaque(false);
        row1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        row1.add(statCard("CPU Scheduling", "2 Algorithms", "FCFS + Round Robin", MainFrame.ACCENT, "\u23f1"));
        row1.add(statCard("Memory Mgmt",    "2 Strategies", "First Fit + Best Fit", MainFrame.ACCENT2, "\ud83e\udde0"));
        row1.add(statCard("Process Sync",   "2 Problems",   "Reader-Writer + Dining", MainFrame.ACCENT3, "\ud83d\udd04"));
        row1.add(statCard("OS Modules",     "4 Active",     "All systems operational", new Color(139,92,246), "\u2699"));
        body.add(row1);
        body.add(Box.createVerticalStrut(14));

        // ── Row 2: Module detail cards (3 columns) ───────────────────────────
        JPanel row2 = new JPanel(new GridLayout(1, 3, 14, 0));
        row2.setOpaque(false);
        row2.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        row2.add(moduleCard("\u23f1  CPU Scheduling", MainFrame.ACCENT,
            new String[]{"FCFS (First Come First Serve)", "Round Robin with configurable quantum",
                         "Gantt Chart visualization", "Avg Wait & Turnaround Time metrics",
                         "Per-process result breakdown"},
            new Color[]{MainFrame.ACCENT2, MainFrame.ACCENT2, MainFrame.ACCENT3, MainFrame.ACCENT3, MainFrame.TEXT_GRAY}));

        row2.add(moduleCard("\ud83e\udde0  Memory Management", MainFrame.ACCENT2,
            new String[]{"First Fit allocation strategy", "Best Fit allocation strategy",
                         "Block-by-block visualization", "Fragmentation analysis",
                         "Dynamic process loading"},
            new Color[]{MainFrame.ACCENT2, MainFrame.ACCENT2, MainFrame.ACCENT3, MainFrame.ACCENT3, MainFrame.TEXT_GRAY}));

        row2.add(moduleCard("\ud83d\udd04  Process Synchronization", MainFrame.ACCENT3,
            new String[]{"Reader-Writer problem simulation", "Dining Philosophers with visuals",
                         "Semaphore-based locking", "Real-time state animation",
                         "Deadlock-free execution log"},
            new Color[]{MainFrame.ACCENT2, MainFrame.ACCENT2, MainFrame.ACCENT3, MainFrame.ACCENT3, MainFrame.TEXT_GRAY}));
        body.add(row2);
        body.add(Box.createVerticalStrut(14));

        // ── Row 3: Quick info bar ─────────────────────────────────────────────
        JPanel row3 = new JPanel(new GridLayout(1, 2, 14, 0));
        row3.setOpaque(false);
        row3.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row3.add(infoCard());
        row3.add(algorithmCard());
        body.add(row3);
        body.add(Box.createVerticalStrut(14));

        // ── Row 4: Group Members ──────────────────────────────────────────────
        body.add(groupMembersCard());
        body.add(Box.createVerticalStrut(14));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Stat card: icon + number + description ───────────────────────────────
    private JPanel statCard(String title, String value, String sub, Color accent, String icon) {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.BG_PANEL);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(accent);
                g2.fillRoundRect(0,0,getWidth(),4,4,4);
                g2.dispose();
            }
        };
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        p.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(MainFrame.TEXT_GRAY);
        top.add(iconLbl, BorderLayout.WEST);
        top.add(titleLbl, BorderLayout.EAST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLbl.setForeground(accent);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLbl.setForeground(MainFrame.TEXT_GRAY);

        p.add(top, BorderLayout.NORTH);
        p.add(valLbl, BorderLayout.CENTER);
        p.add(subLbl, BorderLayout.SOUTH);
        return p;
    }

    // ── Module card with feature bullets ─────────────────────────────────────
    private JPanel moduleCard(String title, Color accent, String[] items, Color[] itemColors) {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.BG_PANEL);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(accent);
                g2.fillRoundRect(0,0,4,getHeight(),4,4);
                g2.dispose();
            }
        };
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 16));
        p.setOpaque(false);

        JLabel tl = new JLabel(title);
        tl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tl.setForeground(accent);
        tl.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        p.add(tl, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (int i = 0; i < items.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setOpaque(false);
            final Color bulletColor = itemColors[Math.min(i, itemColors.length-1)];
            JPanel bull = new JPanel() {
                protected void paintComponent(Graphics g) {
                    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(bulletColor); g.fillOval(0,3,7,7);
                }
                public Dimension getPreferredSize() { return new Dimension(7,14); }
            };
            bull.setOpaque(false);
            JLabel l = new JLabel(items[i]);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(MainFrame.TEXT_GRAY);
            row.add(bull); row.add(l);
            list.add(row);
        }
        p.add(list, BorderLayout.CENTER);
        return p;
    }

    // ── Quick-info card ───────────────────────────────────────────────────────
    private JPanel infoCard() {
        JPanel p = roundedPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));

        JLabel t = new JLabel("\ud83d\udccb  About This Simulator");
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(MainFrame.TEXT_WHITE);
        t.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        p.add(t, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3,2,10,6));
        rows.setOpaque(false);
        rows.add(infoRow("Course", "CSE 3203"));
        rows.add(infoRow("Language", "Java Swing"));
        rows.add(infoRow("Modules", "CPU \u2022 Memory \u2022 Sync"));
        rows.add(infoRow("Version", "v1.0"));
        rows.add(infoRow("Algorithms", "FCFS, RR, FF, BF, Semaphore"));
        rows.add(infoRow("Theme", "Dark UI"));
        p.add(rows, BorderLayout.CENTER);
        return p;
    }

    // ── Algorithm summary card ────────────────────────────────────────────────
    private JPanel algorithmCard() {
        JPanel p = roundedPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(14,16,14,16));

        JLabel t = new JLabel("\u26a1  Implemented Algorithms");
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(MainFrame.TEXT_WHITE);
        t.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        p.add(t, BorderLayout.NORTH);

        JPanel rows = new JPanel(new GridLayout(3,2,10,6));
        rows.setOpaque(false);
        rows.add(algoTag("FCFS",          MainFrame.ACCENT));
        rows.add(algoTag("Round Robin",   MainFrame.ACCENT));
        rows.add(algoTag("First Fit",     MainFrame.ACCENT2));
        rows.add(algoTag("Best Fit",      MainFrame.ACCENT2));
        rows.add(algoTag("Reader-Writer", MainFrame.ACCENT3));
        rows.add(algoTag("Dining Philos.",MainFrame.ACCENT3));
        p.add(rows, BorderLayout.CENTER);
        return p;
    }

    private JPanel infoRow(String key, String val) {
        JPanel p = new JPanel(new BorderLayout(6,0));
        p.setOpaque(false);
        JLabel k = new JLabel(key + ":"); k.setFont(new Font("Segoe UI",Font.PLAIN,11)); k.setForeground(MainFrame.TEXT_GRAY);
        JLabel v = new JLabel(val);       v.setFont(new Font("Segoe UI",Font.BOLD,11));  v.setForeground(MainFrame.TEXT_WHITE);
        p.add(k, BorderLayout.WEST); p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JPanel algoTag(String name, Color accent) {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 100));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                g2.dispose();
            }
        };
        p.setLayout(new FlowLayout(FlowLayout.CENTER,0,3));
        p.setOpaque(false);
        JLabel l = new JLabel(name); l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setForeground(accent);
        p.add(l);
        return p;
    }

    private JPanel roundedPanel() {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MainFrame.BG_PANEL); g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.dispose();
            }
        };
    }

    // ── Group Members Card — 4 members, name + ID, each with unique colour ────
    private JPanel groupMembersCard() {

        // Member data
        final String[] ids     = { "232311299", "232311301", "232311311", "232311198" };
        final String[] names   = { "Bishal",    "Shezan",    "Razibul",   "Borsha"   };
        final String[] inits   = { "Bi",        "Sh",        "Ra",        "Bo"       };
        final Color[]  colors  = {
            new Color( 99, 102, 241),   // indigo  – Bishal
            new Color( 16, 185, 129),   // emerald – Shezan
            new Color(245, 158,  11),   // amber   – Razibul
            new Color(236,  72, 153)    // pink    – Borsha
        };

        // ── Outer container with gradient + border ────────────────────────
        JPanel outer = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(28, 25, 58),
                    getWidth(), 0, new Color(15, 38, 52));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(99, 102, 241, 80));
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setLayout(new BorderLayout(0, 12));
        outer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // ── Section header ────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);

        JLabel iconLbl = new JLabel("\ud83d\udc65  Group Members");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        iconLbl.setForeground(MainFrame.TEXT_WHITE);

        JLabel courseLbl = new JLabel("CSE 3203  \u2014  Operating Systems");
        courseLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        courseLbl.setForeground(MainFrame.TEXT_GRAY);

        hdr.add(iconLbl,  BorderLayout.WEST);
        hdr.add(courseLbl, BorderLayout.EAST);
        outer.add(hdr, BorderLayout.NORTH);

        // ── 4 member cards ────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);

        for (int i = 0; i < 4; i++) {
            final Color  accent = colors[i];
            final String id     = ids[i];
            final String name   = names[i];
            final String init   = inits[i];

            // Card background
            JPanel card = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Tinted fill
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    // Coloured border
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 90));
                    g2.setStroke(new java.awt.BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                    // Top accent line
                    g2.setColor(accent);
                    g2.setStroke(new java.awt.BasicStroke(2.5f));
                    g2.drawLine(14, 0, getWidth()-14, 0);
                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setLayout(new BorderLayout(10, 0));
            card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

            // Avatar circle
            JPanel avatar = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Glow ring
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                    g2.fillOval(0, 0, 42, 42);
                    // Inner circle
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 70));
                    g2.fillOval(3, 3, 36, 36);
                    // Initials
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(init, 21 - fm.stringWidth(init)/2, 26);
                    g2.dispose();
                }
                public Dimension getPreferredSize() { return new Dimension(42, 42); }
            };
            avatar.setOpaque(false);

            // Text info
            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

            JLabel nameLbl = new JLabel(name);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nameLbl.setForeground(accent);

            JLabel idLbl = new JLabel(id);
            idLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            idLbl.setForeground(MainFrame.TEXT_GRAY);

            // Small coloured tag "Member N"
            JLabel tagLbl = new JLabel("Member " + (i + 1));
            tagLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            tagLbl.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180));

            info.add(nameLbl);
            info.add(Box.createVerticalStrut(2));
            info.add(idLbl);
            info.add(Box.createVerticalStrut(3));
            info.add(tagLbl);

            card.add(avatar, BorderLayout.WEST);
            card.add(info,   BorderLayout.CENTER);
            grid.add(card);
        }

        outer.add(grid, BorderLayout.CENTER);
        return outer;
    }
}
