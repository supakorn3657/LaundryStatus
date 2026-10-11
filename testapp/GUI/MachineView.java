package testapp.GUI;

import testapp.Lib.InvalidOperationException;
import testapp.Lib.MachineController;
import testapp.Lib.MachineStatus;
import testapp.Lib.WashMode;
import testapp.Lib.WashingMachine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MachineView extends JFrame {

    final DashboardView dashboard;
    final MachineController controller;
    final String machineId;
    final String loginRoom;

    JLabel clockMac = new JLabel("00:00:00");
    JPanel stage;
    JLabel remainLabel;
    MachineStatus shown;
    boolean closed;

    static final WashMode[] MODES = {WashMode.NORMAL, WashMode.FAST, WashMode.COMFORTER};

    public MachineView(DashboardView dashboard, MachineController controller, String machineId, String loginRoom) {
        this.dashboard = dashboard;
        this.controller = controller;
        this.machineId = machineId;
        this.loginRoom = loginRoom;

        setTitle("สถานะตู้ซักผ้า");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(LoginView.PAPER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                closeBack();
            }
        });

        JPanel root = new JPanel(new BorderLayout(0, 22));
        root.setBackground(LoginView.PAPER);
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        stage = new JPanel(new BorderLayout());
        stage.setOpaque(false);

        root.add(header(), BorderLayout.NORTH);
        root.add(stage, BorderLayout.CENTER);
        setContentPane(root);
        clockMac.setText(dashboard.clockDash.getText());
        showCurrent();
        LoginView.maximize(this);
    }

    JComponent header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LoginView.LINE),
                BorderFactory.createEmptyBorder(8, 0, 18, 0)
        ));

        JLabel title = new JLabel(machineId);
        title.setFont(LoginView.uiFont(Font.BOLD, 32));
        title.setForeground(LoginView.INK);

        JLabel who = new JLabel("ห้องที่เข้าใช้ " + loginRoom);
        who.setFont(LoginView.uiFont(Font.PLAIN, 14));
        who.setForeground(LoginView.MUTED);
        who.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        who.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);
        left.add(who);

        JLabel cap = new JLabel("เวลา");
        cap.setFont(LoginView.uiFont(Font.PLAIN, 12));
        cap.setForeground(LoginView.MUTED);
        cap.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockMac.setFont(LoginView.clockFont(34));
        clockMac.setForeground(LoginView.INK);
        clockMac.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel clockWrap = new JPanel();
        clockWrap.setOpaque(false);
        clockWrap.setLayout(new BoxLayout(clockWrap, BoxLayout.Y_AXIS));
        clockWrap.add(cap);
        clockWrap.add(clockMac);

        header.add(left, BorderLayout.WEST);
        header.add(clockWrap, BorderLayout.EAST);
        return header;
    }

    public boolean isSameMachine(String id) {
        return machineId.equals(id);
    }

    public void setClock(String text) {
        clockMac.setText(text);
    }

    public void tick(WashingMachine machine) {
        if (closed || shown != machine.getStatus() || remainLabel == null) {
            return;
        }
        remainLabel.setText(DashboardView.formatRemain(machine.getTimeRemaining()));
    }

    public void showCurrent() {
        if (closed) {
            return;
        }
        WashingMachine machine = controller.findMachine(machineId);
        if (machine == null) {
            return;
        }
        shown = machine.getStatus();
        remainLabel = null;

        LoginView.RoundPanel identity = identityCard(machine);
        LoginView.RoundPanel panel = actionCard(machine);

        JPanel row = new JPanel(new GridLayout(1, 2, 22, 0));
        row.setOpaque(false);
        row.add(identity);
        row.add(panel);

        stage.removeAll();
        stage.add(row, BorderLayout.CENTER);
        stage.revalidate();
        stage.repaint();
    }

    LoginView.RoundPanel identityCard(WashingMachine machine) {
        LoginView.RoundPanel card = new LoginView.RoundPanel(22);
        card.setLayout(new GridBagLayout());

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        LoginView.WasherIcon icon = new LoginView.WasherIcon(DashboardView.statusFg(machine.getStatus()), 120);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(machine.getMachineId());
        name.setFont(LoginView.uiFont(Font.BOLD, 56));
        name.setForeground(LoginView.INK);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel chip = LoginView.badge(
                DashboardView.statusText(machine.getStatus()),
                DashboardView.statusFg(machine.getStatus()),
                DashboardView.statusBg(machine.getStatus())
        );
        chip.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel room = new JLabel("ห้องบนเครื่อง " + DashboardView.roomText(machine));
        room.setFont(LoginView.uiFont(Font.PLAIN, 22));
        room.setForeground(LoginView.MUTED);
        room.setAlignmentX(Component.CENTER_ALIGNMENT);

        stack.add(icon);
        stack.add(Box.createVerticalStrut(22));
        stack.add(name);
        stack.add(Box.createVerticalStrut(16));
        stack.add(chip);
        stack.add(Box.createVerticalStrut(14));
        stack.add(room);
        card.add(stack);
        return card;
    }

    LoginView.RoundPanel actionCard(final WashingMachine machine) {
        LoginView.RoundPanel card = new LoginView.RoundPanel(22);
        card.setLayout(new GridBagLayout());

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        if (machine.getStatus() == MachineStatus.AVAILABLE) {
            stack.add(heading("เลือกโหมด"));
            stack.add(note("เวลาซักตามโหมดที่เลือก"));
            stack.add(Box.createVerticalStrut(12));
            stack.add(modeList());
            stack.add(Box.createVerticalStrut(12));
            stack.add(backLink());
            stack.add(Box.createVerticalStrut(18));
            stack.add(startButton());
        } else if (machine.getStatus() == MachineStatus.IN_USE) {
            String mode = machine.getCurrentMode() == null ? "--" : machine.getCurrentMode().getLabel();
            stack.add(heading("โหมด " + mode));
            remainLabel = bigTime(DashboardView.formatRemain(machine.getTimeRemaining()));
            stack.add(remainLabel);
            stack.add(note("ซักเสร็จประมาณ " + finishClock(machine)));
            stack.add(Box.createVerticalStrut(18));
            stack.add(backGhost());
        } else if (loginRoom.equals(machine.getCurrentRoomNo())) {
            String mode = machine.getCurrentMode() == null ? "--" : machine.getCurrentMode().getLabel();
            stack.add(heading("โหมด " + mode));
            remainLabel = bigTime(DashboardView.formatRemain(machine.getTimeRemaining()));
            stack.add(remainLabel);
            stack.add(note("เหลือเวลาเก็บผ้า"));
            stack.add(Box.createVerticalStrut(12));
            stack.add(backLink());
            stack.add(Box.createVerticalStrut(18));
            JButton collect = LoginView.actionButton("เก็บผ้า", LoginView.YELLOW, LoginView.CREAM);
            collect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        controller.collectClothes(machineId);
                        dispose();
                    } catch (InvalidOperationException ex) {
                        JOptionPane.showMessageDialog(MachineView.this, ex.getMessage());
                    }
                }
            });
            stack.add(collect);
        } else {
            stack.add(heading("ห้อง " + DashboardView.roomText(machine) + " เป็นคนเริ่มซัก"));
            remainLabel = bigTime(DashboardView.formatRemain(machine.getTimeRemaining()));
            stack.add(remainLabel);
            stack.add(note("ห้อง " + loginRoom + " เก็บผ้านี้ไม่ได้"));
            stack.add(Box.createVerticalStrut(18));
            stack.add(backGhost());
        }

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(36, 40, 36, 40);
        card.add(stack, constraints);
        return card;
    }

    JComponent modeList() {
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup group = new ButtonGroup();
        final JRadioButton[] radios = new JRadioButton[MODES.length];
        for (int i = 0; i < MODES.length; i++) {
            radios[i] = modeRow(list, group, MODES[i]);
        }
        list.putClientProperty("radios", radios);
        this.modeRadios = radios;
        return list;
    }

    JRadioButton[] modeRadios;

    JRadioButton modeRow(JPanel list, ButtonGroup group, final WashMode mode) {
        final JRadioButton radio = new JRadioButton(mode.getLabel());
        radio.setOpaque(false);
        radio.setFont(LoginView.uiFont(Font.PLAIN, 22));
        radio.setForeground(LoginView.INK);
        radio.setFocusPainted(false);
        group.add(radio);

        JLabel mins = new JLabel(mode.getSeconds() + " วินาที");
        mins.setFont(LoginView.uiFont(Font.PLAIN, 20));
        mins.setForeground(LoginView.MUTED);

        final JPanel row = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean on = radio.isSelected();
                g2.setColor(on ? LoginView.SELECT : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(on ? LoginView.BRASS : LoginView.LINE);
                g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 18));
        row.add(radio, BorderLayout.WEST);
        row.add(mins, BorderLayout.EAST);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                radio.setSelected(true);
            }
        });
        radio.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                row.repaint();
            }
        });
        list.add(row);
        list.add(Box.createVerticalStrut(8));
        return radio;
    }

    JButton startButton() {
        JButton start = LoginView.actionButton("เริ่มซัก", LoginView.INK, LoginView.CREAM);
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int pick = -1;
                if (modeRadios != null) {
                    for (int i = 0; i < modeRadios.length; i++) {
                        if (modeRadios[i].isSelected()) {
                            pick = i;
                        }
                    }
                }
                if (pick < 0) {
                    JOptionPane.showMessageDialog(MachineView.this, "กรุณาเลือกโหมดก่อน");
                    return;
                }
                try {
                    controller.startWash(machineId, loginRoom, MODES[pick]);
                } catch (InvalidOperationException ex) {
                    JOptionPane.showMessageDialog(MachineView.this, ex.getMessage());
                }
            }
        });
        return start;
    }

    JButton backGhost() {
        JButton back = LoginView.ghostButton("กลับ");
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        return back;
    }

    JButton backLink() {
        JButton back = LoginView.linkButton("กลับไปตู้ทั้งหมด");
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        return back;
    }

    static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LoginView.uiFont(Font.BOLD, 28));
        label.setForeground(LoginView.INK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JLabel note(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LoginView.uiFont(Font.PLAIN, 22));
        label.setForeground(LoginView.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        return label;
    }

    static JLabel bigTime(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LoginView.clockFont(84));
        label.setForeground(LoginView.INK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static String finishClock(WashingMachine machine) {
        if (machine.getExpectedFinish() == null) {
            return "--:--";
        }
        return machine.getExpectedFinish().format(DashboardView.HOUR);
    }

    void closeBack() {
        if (closed) {
            return;
        }
        closed = true;
        dashboard.machineClosed();
    }

    public static void main(String[] args) {
        LoginView.main(args);
    }
}
