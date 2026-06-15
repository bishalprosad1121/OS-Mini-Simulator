import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class CPUSchedulingPanel extends JPanel {

    private DefaultTableModel procModel;
    private JComboBox<String> algoBox;
    private JTextField quantumField;
    private JPanel ganttPanel;
    private JLabel avgWaitLbl, avgTurnLbl, cpuUtilLbl;
    private List<int[]> ganttBlocks = new ArrayList<>(); // [pid, start, end]
    private JTable resultTable;
    private DefaultTableModel resultModel;

    static final Color[] PROC_COLORS = {
        new Color(99,102,241), new Color(16,185,129), new Color(245,158,11),
        new Color(239,68,68),  new Color(139,92,246), new Color(20,184,166),
        new Color(251,146,60), new Color(236,72,153)
    };

    public CPUSchedulingPanel() {
        setBackground(MainFrame.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel title = MainFrame.headerLabel("⏱  CPU Scheduling Simulator");
        JLabel sub = MainFrame.subLabel("FCFS and Round Robin algorithms with Gantt Chart visualization");
        JPanel hdr = new JPanel(); hdr.setOpaque(false);
        hdr.setLayout(new BoxLayout(hdr, BoxLayout.Y_AXIS));
        hdr.add(title); hdr.add(Box.createVerticalStrut(4)); hdr.add(sub);
        add(hdr, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        center.add(buildLeftPanel(), BorderLayout.WEST);
        center.add(buildRightPanel(), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(310, 0));

        // Algorithm selection
        JPanel algoCard = card("Algorithm Settings");
        algoCard.setLayout(new BoxLayout(algoCard, BoxLayout.Y_AXIS));

        algoBox = new JComboBox<>(new String[]{"FCFS", "Round Robin"});
        algoBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        algoBox.setBackground(MainFrame.BG_CARD);
        algoBox.setForeground(MainFrame.TEXT_WHITE);
        algoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JPanel qRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        qRow.setOpaque(false);
        JLabel qLbl = new JLabel("Time Quantum: ");
        qLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        qLbl.setForeground(MainFrame.TEXT_GRAY);
        quantumField = MainFrame.styledField(4);
        quantumField.setText("2");
        quantumField.setMaximumSize(new Dimension(60, 28));
        qRow.add(qLbl); qRow.add(quantumField);

        algoBox.addActionListener(e -> quantumField.setEnabled(algoBox.getSelectedIndex()==1));
        quantumField.setEnabled(false);

        algoCard.add(fieldRow("Algorithm:", algoBox));
        algoCard.add(Box.createVerticalStrut(6));
        algoCard.add(qRow);
        p.add(algoCard);
        p.add(Box.createVerticalStrut(12));

        // Process table
        JPanel procCard = card("Process Table");
        procCard.setLayout(new BorderLayout());
        String[] cols = {"PID","Arrival","Burst","Priority"};
        procModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return true; }
        };
        addDefaultProcs();
        JTable table = new JTable(procModel);
        styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(0, 180));
        sp.setBackground(MainFrame.BG_PANEL);
        sp.getViewport().setBackground(MainFrame.BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(MainFrame.BG_CARD));
        procCard.add(sp, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        btnRow.setOpaque(false);
        JButton addBtn = MainFrame.styledBtn("+ Add", MainFrame.ACCENT2);
        addBtn.setPreferredSize(new Dimension(80, 28));
        addBtn.addActionListener(e -> {
            int n = procModel.getRowCount()+1;
            procModel.addRow(new Object[]{"P"+n, n-1, (int)(Math.random()*8)+2, n});
        });
        JButton delBtn = MainFrame.styledBtn("Remove", MainFrame.DANGER);
        delBtn.setPreferredSize(new Dimension(80, 28));
        delBtn.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) procModel.removeRow(r);
        });
        JButton resetBtn = MainFrame.styledBtn("Reset", MainFrame.BG_CARD);
        resetBtn.setPreferredSize(new Dimension(70, 28));
        resetBtn.addActionListener(e -> { procModel.setRowCount(0); addDefaultProcs(); });
        btnRow.add(addBtn); btnRow.add(delBtn); btnRow.add(resetBtn);
        procCard.add(btnRow, BorderLayout.SOUTH);
        p.add(procCard);
        p.add(Box.createVerticalStrut(12));

        JButton runBtn = MainFrame.styledBtn("▶  Run Simulation", MainFrame.ACCENT);
        runBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        runBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        runBtn.addActionListener(e -> runSimulation());
        p.add(runBtn);
        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // Gantt
        JPanel ganttCard = card("Gantt Chart");
        ganttCard.setLayout(new BorderLayout());
        ganttPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGantt((Graphics2D)g);
            }
        };
        ganttPanel.setBackground(MainFrame.BG_CARD);
        ganttPanel.setPreferredSize(new Dimension(0, 90));
        ganttCard.add(ganttPanel, BorderLayout.CENTER);
        p.add(ganttCard);
        p.add(Box.createVerticalStrut(12));

        // Metrics
        JPanel metricsCard = card("Performance Metrics");
        metricsCard.setLayout(new GridLayout(1, 3, 12, 0));
        avgWaitLbl = metricLabel("Avg Wait Time", "—");
        avgTurnLbl = metricLabel("Avg Turnaround", "—");
        cpuUtilLbl = metricLabel("CPU Utilization", "—");
        metricsCard.add(avgWaitLbl.getParent());
        metricsCard.add(avgTurnLbl.getParent());
        metricsCard.add(cpuUtilLbl.getParent());
        p.add(metricsCard);
        p.add(Box.createVerticalStrut(12));

        // Result table
        JPanel resultCard = card("Process Results");
        resultCard.setLayout(new BorderLayout());
        String[] cols = {"PID","Arrival","Burst","Finish","Wait Time","Turnaround"};
        resultModel = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        resultTable = new JTable(resultModel);
        styleTable(resultTable);
        JScrollPane sp = new JScrollPane(resultTable);
        sp.setBackground(MainFrame.BG_PANEL);
        sp.getViewport().setBackground(MainFrame.BG_PANEL);
        sp.setBorder(BorderFactory.createLineBorder(MainFrame.BG_CARD));
        resultCard.add(sp, BorderLayout.CENTER);
        p.add(resultCard);
        return p;
    }

    private void addDefaultProcs() {
        Object[][] data = {
            {"P1",0,5,2}, {"P2",1,3,1}, {"P3",2,8,3}, {"P4",3,6,2}, {"P5",4,4,1}
        };
        for (Object[] row : data) procModel.addRow(row);
    }

    private void runSimulation() {
        int n = procModel.getRowCount();
        if (n == 0) return;
        int[] pid = new int[n], arr = new int[n], burst = new int[n], prio = new int[n];
        try {
            for (int i = 0; i < n; i++) {
                pid[i]   = i;
                arr[i]   = Integer.parseInt(procModel.getValueAt(i,1).toString().trim());
                burst[i] = Integer.parseInt(procModel.getValueAt(i,2).toString().trim());
                prio[i]  = Integer.parseInt(procModel.getValueAt(i,3).toString().trim());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"Invalid input. Check process table.","Error",JOptionPane.ERROR_MESSAGE);
            return;
        }

        ganttBlocks.clear();
        int[] finish = new int[n], wait = new int[n], turn = new int[n];

        if (algoBox.getSelectedIndex() == 0) {
            // FCFS
            int[] order = sortByArrival(arr, n);
            int time = 0;
            for (int idx : order) {
                if (time < arr[idx]) time = arr[idx];
                ganttBlocks.add(new int[]{idx, time, time + burst[idx]});
                time += burst[idx];
                finish[idx] = time;
                turn[idx] = finish[idx] - arr[idx];
                wait[idx] = turn[idx] - burst[idx];
            }
        } else {
            // Round Robin
            int q = 2;
            try { q = Integer.parseInt(quantumField.getText().trim()); } catch (Exception ignored) {}
            int[] rem = Arrays.copyOf(burst, n);
            int time = 0, done = 0;
            while (done < n) {
                boolean any = false;
                for (int i = 0; i < n; i++) {
                    if (rem[i] > 0 && arr[i] <= time) {
                        any = true;
                        int exec = Math.min(rem[i], q);
                        ganttBlocks.add(new int[]{i, time, time + exec});
                        time += exec;
                        rem[i] -= exec;
                        if (rem[i] == 0) {
                            finish[i] = time;
                            turn[i] = finish[i] - arr[i];
                            wait[i] = turn[i] - burst[i];
                            done++;
                        }
                    }
                }
                if (!any) time++;
            }
        }

        // Update results
        resultModel.setRowCount(0);
        double sumWait = 0, sumTurn = 0;
        int totalIdle = 0, totalTime = finish[0];
        for (int i = 0; i < n; i++) {
            totalTime = Math.max(totalTime, finish[i]);
            String name = procModel.getValueAt(i,0).toString();
            resultModel.addRow(new Object[]{name, arr[i], burst[i], finish[i], wait[i], turn[i]});
            sumWait += wait[i]; sumTurn += turn[i];
        }
        avgWaitLbl.setText(String.format("%.2f", sumWait/n));
        avgTurnLbl.setText(String.format("%.2f", sumTurn/n));
        int busyTime = 0;
        for (int[] b : ganttBlocks) busyTime += b[2]-b[1];
        cpuUtilLbl.setText(String.format("%.1f%%", 100.0*busyTime/totalTime));
        ganttPanel.repaint();
    }

    private void drawGantt(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (ganttBlocks.isEmpty()) {
            g2.setColor(MainFrame.TEXT_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            g2.drawString("Run simulation to see Gantt chart", 20, ganttPanel.getHeight()/2+5);
            return;
        }
        int maxT = ganttBlocks.stream().mapToInt(b->b[2]).max().orElse(1);
        int w = ganttPanel.getWidth()-40, h = ganttPanel.getHeight()-30, y = 14;
        float scale = (float)w / maxT;
        for (int[] b : ganttBlocks) {
            int x = 20+(int)(b[1]*scale), bw = Math.max(2,(int)((b[2]-b[1])*scale));
            Color c = PROC_COLORS[b[0] % PROC_COLORS.length];
            g2.setColor(c);
            g2.fillRoundRect(x, y, bw-2, h-4, 6, 6);
            g2.setColor(new Color(0,0,0,60));
            g2.fillRoundRect(x, y+h/2, bw-2, h/2-4, 6, 6);
            if (bw > 22) {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String lbl = procModel.getValueAt(b[0],0).toString();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(lbl, x+(bw-fm.stringWidth(lbl))/2, y+h/2-1);
            }
            // time labels
            g2.setColor(MainFrame.TEXT_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(String.valueOf(b[1]), x, y+h+12);
        }
        int lastT = ganttBlocks.get(ganttBlocks.size()-1)[2];
        g2.setColor(MainFrame.TEXT_GRAY);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.drawString(String.valueOf(lastT), 20+(int)(lastT*scale), y+h+12);
    }

    private int[] sortByArrival(int[] arr, int n) {
        Integer[] idx = new Integer[n];
        for (int i=0;i<n;i++) idx[i]=i;
        Arrays.sort(idx,(a,b)->arr[a]-arr[b]);
        return Arrays.stream(idx).mapToInt(Integer::intValue).toArray();
    }

    private JPanel card(String title) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        JPanel inner = MainFrame.roundedCard(MainFrame.BG_PANEL, 10);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(MainFrame.TEXT_GRAY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        inner.add(lbl);
        outer.add(inner, BorderLayout.CENTER);
        return inner;
    }

    private JPanel fieldRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8,0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(MainFrame.TEXT_GRAY);
        lbl.setPreferredSize(new Dimension(100,28));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JLabel metricLabel(String title, String val) {
        JPanel card = MainFrame.roundedCard(MainFrame.BG_CARD, 8);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(MainFrame.TEXT_GRAY);
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 22));
        v.setForeground(MainFrame.TEXT_WHITE);
        card.add(t); card.add(Box.createVerticalStrut(4)); card.add(v);
        return v;
    }

    private void styleTable(JTable t) {
        t.setBackground(MainFrame.BG_PANEL);
        t.setForeground(MainFrame.TEXT_WHITE);
        t.setGridColor(MainFrame.BG_CARD);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setRowHeight(26);
        t.setShowGrid(true);
        t.getTableHeader().setBackground(MainFrame.BG_CARD);
        t.getTableHeader().setForeground(MainFrame.TEXT_GRAY);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        t.setSelectionBackground(new Color(MainFrame.ACCENT.getRed(), MainFrame.ACCENT.getGreen(), MainFrame.ACCENT.getBlue(), 80));
        t.setSelectionForeground(MainFrame.TEXT_WHITE);
    }
}
