import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MemoryManagementPanel extends JPanel {

    private DefaultTableModel blockModel, procModel;
    private JComboBox<String> algoBox;
    private JPanel visualPanel;
    private DefaultTableModel resultModel;
    private JLabel fragLbl, allocLbl, freeLbl;

    // allocation result: index = process index, value = block index (-1=not allocated)
    private int[] allocation;
    private int[] blockSizes;
    private int[] procSizes;

    static final Color FREE_COLOR    = new Color(51, 65, 85);
    static final Color ALLOC_COLORS[] = {
        new Color(99,102,241), new Color(16,185,129), new Color(245,158,11),
        new Color(239,68,68),  new Color(139,92,246), new Color(20,184,166)
    };

    public MemoryManagementPanel() {
        setBackground(MainFrame.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = MainFrame.headerLabel("🧠  Memory Management");
        JLabel sub = MainFrame.subLabel("First Fit and Best Fit allocation with memory visualization");
        JPanel hdr = new JPanel(); hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        add(hdr, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16,0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));
        center.add(buildLeftPanel(), BorderLayout.WEST);
        center.add(buildRightPanel(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(280, 0));

        // Algorithm
        JPanel algoCard = card("Settings");
        algoBox = new JComboBox<>(new String[]{"First Fit","Best Fit"});
        algoBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        algoBox.setBackground(MainFrame.BG_CARD);
        algoBox.setForeground(MainFrame.TEXT_WHITE);
        algoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        algoCard.add(new JLabel("Algorithm:") {{ setFont(new Font("Segoe UI",Font.PLAIN,11)); setForeground(MainFrame.TEXT_GRAY); }});
        algoCard.add(Box.createVerticalStrut(4));
        algoCard.add(algoBox);
        p.add(algoCard);
        p.add(Box.createVerticalStrut(10));

        // Memory blocks
        JPanel blockCard = card("Memory Blocks (KB)");
        blockCard.setLayout(new BorderLayout());
        String[] bc = {"Block","Size (KB)"};
        blockModel = new DefaultTableModel(bc, 0);
        Object[][] blocks = {{"B1",100},{"B2",500},{"B3",200},{"B4",300},{"B5",600},{"B6",150}};
        for (Object[] r : blocks) blockModel.addRow(r);
        JTable bt = new JTable(blockModel);
        styleTable(bt);
        JScrollPane bs = new JScrollPane(bt);
        bs.setPreferredSize(new Dimension(0, 150));
        styleScrollPane(bs);
        blockCard.add(bs, BorderLayout.CENTER);
        JPanel bBtns = new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        bBtns.setOpaque(false);
        JButton addB = MainFrame.styledBtn("+Block", MainFrame.ACCENT2);
        addB.setPreferredSize(new Dimension(75,26));
        addB.addActionListener(e -> blockModel.addRow(new Object[]{"B"+(blockModel.getRowCount()+1),(int)(Math.random()*400)+100}));
        JButton delB = MainFrame.styledBtn("Del", MainFrame.DANGER);
        delB.setPreferredSize(new Dimension(50,26));
        delB.addActionListener(e -> { int r=bt.getSelectedRow(); if(r>=0) blockModel.removeRow(r); });
        bBtns.add(addB); bBtns.add(delB);
        blockCard.add(bBtns, BorderLayout.SOUTH);
        p.add(blockCard);
        p.add(Box.createVerticalStrut(10));

        // Process sizes
        JPanel procCard = card("Processes to Allocate (KB)");
        procCard.setLayout(new BorderLayout());
        String[] pc = {"Process","Size (KB)"};
        procModel = new DefaultTableModel(pc, 0);
        Object[][] procs = {{"P1",212},{"P2",417},{"P3",112},{"P4",426}};
        for (Object[] r : procs) procModel.addRow(r);
        JTable pt = new JTable(procModel);
        styleTable(pt);
        JScrollPane ps = new JScrollPane(pt);
        ps.setPreferredSize(new Dimension(0, 120));
        styleScrollPane(ps);
        procCard.add(ps, BorderLayout.CENTER);
        JPanel pBtns = new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        pBtns.setOpaque(false);
        JButton addP = MainFrame.styledBtn("+Proc", MainFrame.ACCENT2);
        addP.setPreferredSize(new Dimension(75,26));
        addP.addActionListener(e -> procModel.addRow(new Object[]{"P"+(procModel.getRowCount()+1),(int)(Math.random()*300)+100}));
        JButton delP = MainFrame.styledBtn("Del", MainFrame.DANGER);
        delP.setPreferredSize(new Dimension(50,26));
        delP.addActionListener(e -> { int r=pt.getSelectedRow(); if(r>=0) procModel.removeRow(r); });
        pBtns.add(addP); pBtns.add(delP);
        procCard.add(pBtns, BorderLayout.SOUTH);
        p.add(procCard);
        p.add(Box.createVerticalStrut(10));

        JButton run = MainFrame.styledBtn("▶  Allocate", MainFrame.ACCENT);
        run.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        run.setFont(new Font("Segoe UI", Font.BOLD, 13));
        run.addActionListener(e -> runAllocation());
        p.add(run);
        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // Visual
        JPanel visCard = card("Memory Block Visualization");
        visCard.setLayout(new BorderLayout());
        visualPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawMemory((Graphics2D)g);
            }
        };
        visualPanel.setBackground(MainFrame.BG_CARD);
        visualPanel.setPreferredSize(new Dimension(0, 160));
        visCard.add(visualPanel, BorderLayout.CENTER);
        p.add(visCard);
        p.add(Box.createVerticalStrut(10));

        // Metrics
        JPanel metrics = new JPanel(new GridLayout(1,3,10,0));
        metrics.setOpaque(false);
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        allocLbl = metricVal("Allocated", "—");
        freeLbl  = metricVal("Free Blocks", "—");
        fragLbl  = metricVal("Fragmentation", "—");
        metrics.add(allocLbl.getParent()); metrics.add(freeLbl.getParent()); metrics.add(fragLbl.getParent());
        p.add(metrics);
        p.add(Box.createVerticalStrut(10));

        // Result table
        JPanel resCard = card("Allocation Results");
        resCard.setLayout(new BorderLayout());
        String[] cols = {"Process","Size (KB)","Allocated Block","Block Size","Status","Wasted"};
        resultModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        JTable rt = new JTable(resultModel);
        styleTable(rt);
        JScrollPane rs = new JScrollPane(rt);
        styleScrollPane(rs);
        resCard.add(rs, BorderLayout.CENTER);
        p.add(resCard);
        return p;
    }

    private void runAllocation() {
        int nb = blockModel.getRowCount(), np = procModel.getRowCount();
        if (nb == 0 || np == 0) return;
        blockSizes = new int[nb]; procSizes = new int[np];
        try {
            for (int i=0;i<nb;i++) blockSizes[i] = Integer.parseInt(blockModel.getValueAt(i,1).toString().trim());
            for (int i=0;i<np;i++) procSizes[i]  = Integer.parseInt(procModel.getValueAt(i,1).toString().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Invalid sizes","Error",JOptionPane.ERROR_MESSAGE); return;
        }

        int[] rem = Arrays.copyOf(blockSizes, nb);
        allocation = new int[np];
        Arrays.fill(allocation, -1);

        if (algoBox.getSelectedIndex() == 0) {
            // First Fit
            for (int i=0;i<np;i++) {
                for (int j=0;j<nb;j++) {
                    if (rem[j] >= procSizes[i]) { allocation[i]=j; rem[j]-=procSizes[i]; break; }
                }
            }
        } else {
            // Best Fit
            for (int i=0;i<np;i++) {
                int best = -1;
                for (int j=0;j<nb;j++) {
                    if (rem[j] >= procSizes[i]) {
                        if (best == -1 || rem[j] < rem[best]) best = j;
                    }
                }
                if (best != -1) { allocation[i]=best; rem[best]-=procSizes[i]; }
            }
        }

        // Results table
        resultModel.setRowCount(0);
        int allocCount=0; long wasted=0;
        for (int i=0;i<np;i++) {
            String pn = procModel.getValueAt(i,0).toString();
            if (allocation[i]==-1) {
                resultModel.addRow(new Object[]{pn, procSizes[i],"—","—","Not Allocated","—"});
            } else {
                String bn = blockModel.getValueAt(allocation[i],0).toString();
                int waste = rem[allocation[i]]; // remaining after our allocation step
                resultModel.addRow(new Object[]{pn, procSizes[i], bn, blockSizes[allocation[i]],"Allocated", waste});
                allocCount++; wasted += waste;
            }
        }

        allocLbl.setText(allocCount+"/"+np);
        freeLbl.setText((nb - new java.util.HashSet<>(java.util.Arrays.asList(
            java.util.Arrays.stream(allocation).boxed().filter(x->x!=-1).toArray(Integer[]::new))).size())+" blocks");
        fragLbl.setText(wasted+" KB");
        visualPanel.repaint();
    }

    private void drawMemory(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (blockSizes == null) {
            g2.setColor(MainFrame.TEXT_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString("Run allocation to see memory visualization", 20, visualPanel.getHeight()/2);
            return;
        }
        int nb = blockSizes.length;
        int w = visualPanel.getWidth() - 40, h = visualPanel.getHeight() - 40;
        int blockW = w/nb - 8;
        int x = 20;
        for (int b=0;b<nb;b++) {
            // find process in this block
            int procIdx = -1;
            for (int i=0;i<allocation.length;i++) if (allocation[i]==b) { procIdx=i; break; }

            Color c = (procIdx >= 0) ? ALLOC_COLORS[procIdx % ALLOC_COLORS.length] : FREE_COLOR;
            int bh = (int)(0.8 * h * blockSizes[b] / Arrays.stream(blockSizes).max().getAsInt());
            int y = h - bh + 10;
            g2.setColor(c);
            g2.fillRoundRect(x, y, blockW, bh, 8, 8);
            if (procIdx >= 0) {
                g2.setColor(new Color(0,0,0,50));
                g2.fillRoundRect(x, y+bh/2, blockW, bh/2, 8, 8);
            }
            // labels
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            String bn = blockModel.getValueAt(b,0).toString();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(bn, x+(blockW-fm.stringWidth(bn))/2, y+14);
            if (procIdx >= 0) {
                String pn = procModel.getValueAt(procIdx,0).toString();
                g2.drawString(pn, x+(blockW-fm.stringWidth(pn))/2, y+bh/2+14);
            }
            g2.setColor(MainFrame.TEXT_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            String sz = blockSizes[b]+"KB";
            g2.drawString(sz, x+(blockW-fm.stringWidth(sz))/2, h+26);
            x += blockW + 8;
        }
    }

    private JPanel card(String title) {
        JPanel inner = MainFrame.roundedCard(MainFrame.BG_PANEL, 10);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        inner.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(MainFrame.TEXT_GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        inner.add(lbl);
        return inner;
    }

    private JLabel metricVal(String title, String val) {
        JPanel card = MainFrame.roundedCard(MainFrame.BG_CARD, 8);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(MainFrame.TEXT_GRAY);
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 20));
        v.setForeground(MainFrame.TEXT_WHITE);
        card.add(t); card.add(Box.createVerticalStrut(4)); card.add(v);
        return v;
    }

    private void styleTable(JTable t) {
        t.setBackground(MainFrame.BG_PANEL);
        t.setForeground(MainFrame.TEXT_WHITE);
        t.setGridColor(MainFrame.BG_CARD);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(24);
        t.getTableHeader().setBackground(MainFrame.BG_CARD);
        t.getTableHeader().setForeground(MainFrame.TEXT_GRAY);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setSelectionBackground(new Color(MainFrame.ACCENT.getRed(),MainFrame.ACCENT.getGreen(),MainFrame.ACCENT.getBlue(),80));
        t.setSelectionForeground(MainFrame.TEXT_WHITE);
    }

    private void styleScrollPane(JScrollPane sp) {
        sp.setBackground(MainFrame.BG_PANEL);
        sp.getViewport().setBackground(MainFrame.BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(MainFrame.BG_CARD));
    }
}
