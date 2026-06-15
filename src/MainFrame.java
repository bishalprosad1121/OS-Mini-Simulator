import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private JPanel sidebar;

    static final Color BG_DARK    = new Color(15, 23, 42);
    static final Color BG_PANEL   = new Color(30, 41, 59);
    static final Color BG_CARD    = new Color(51, 65, 85);
    static final Color ACCENT     = new Color(99, 102, 241);
    static final Color ACCENT2    = new Color(16, 185, 129);
    static final Color ACCENT3    = new Color(245, 158, 11);
    static final Color TEXT_WHITE = new Color(248, 250, 252);
    static final Color TEXT_GRAY  = new Color(148, 163, 184);
    static final Color DANGER     = new Color(239, 68, 68);

    public MainFrame() {
        setTitle("Mini OS Simulator — CSE 3203");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        buildSidebar();
        buildContent();
    }

    private void buildSidebar() {
        sidebar = new JPanel();
        sidebar.setBackground(BG_PANEL);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(210, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BG_CARD));

        JLabel logo = new JLabel("⚙  OS Simulator");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logo.setForeground(TEXT_WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(22, 18, 10, 18));
        sidebar.add(logo);

        JLabel sub = new JLabel("CSE 3203");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_GRAY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 18));
        sidebar.add(sub);

        sidebar.add(sectionLabel("MODULES"));
        addNavBtn("🏠  Dashboard",       "dashboard");
        addNavBtn("⏱  CPU Scheduling",   "cpu");
        addNavBtn("🧠  Memory Management","memory");
        addNavBtn("🔄  Process Sync",     "sync");

        sidebar.add(Box.createVerticalGlue());
        JLabel footer = new JLabel("v1.0  •  Java Swing");
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        footer.setForeground(TEXT_GRAY);
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 18, 16, 0));
        sidebar.add(footer);

        add(sidebar, BorderLayout.WEST);
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(TEXT_GRAY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(14, 18, 6, 18));
        return lbl;
    }

    private ButtonGroup navGroup = new ButtonGroup();

    private void addNavBtn(String label, String card) {
        JToggleButton btn = new JToggleButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected()) {
                    g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 40));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 8, 3, getHeight()-16, 2, 2);
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,12));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(TEXT_GRAY);
        btn.setBackground(new Color(0,0,0,0));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(210, 40));
        btn.setPreferredSize(new Dimension(210, 40));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        btn.addActionListener(e -> {
            cardLayout.show(contentPanel, card);
            btn.setForeground(TEXT_WHITE);
        });
        btn.addChangeListener(e -> {
            if (!btn.isSelected()) btn.setForeground(TEXT_GRAY);
        });
        navGroup.add(btn);
        sidebar.add(btn);

        if (card.equals("dashboard")) {
            btn.setSelected(true);
            btn.setForeground(TEXT_WHITE);
        }
    }

    private void buildContent() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG_DARK);
        contentPanel.add(new DashboardPanel(), "dashboard");
        contentPanel.add(new CPUSchedulingPanel(), "cpu");
        contentPanel.add(new MemoryManagementPanel(), "memory");
        contentPanel.add(new SynchronizationPanel(), "sync");
        add(contentPanel, BorderLayout.CENTER);
    }

    // Utility: styled rounded panel
    public static JPanel roundedCard(Color bg, int arc) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }
        };
    }

    public static JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? bg.darker() :
                          getModel().isRollover() ? bg.brighter() : bg;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(100, 32));
        return b;
    }

    public static JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setForeground(TEXT_WHITE);
        f.setBackground(BG_CARD);
        f.setCaretColor(TEXT_WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return f;
    }

    public static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        l.setForeground(TEXT_WHITE);
        return l;
    }

    public static JLabel subLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_GRAY);
        return l;
    }
}
