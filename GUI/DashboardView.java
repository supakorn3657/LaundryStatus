package GUI;

import Lib.MachineController;
import Lib.MachineListener;
import Lib.MachineStatus;
import Lib.WashingMachine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardView extends JFrame implements MachineListener {

    static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH:mm");

    final LoginView login;
    final MachineController controller;
    String roomNo;
    JLabel roomWho;
    JLabel clockDash = new JLabel("00:00:00");
    JPanel gridPanel;
    CardRefs[] refs;
    MachineView openView;

    public DashboardView(LoginView login, MachineController controller, String roomNo) {
        this.login = login;
        this.controller = controller;
        this.roomNo = roomNo;

        setTitle("สถานะตู้ซักผ้า");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(LoginView.PAPER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.shutdown();
            }
        });

        JPanel root = new JPanel(new BorderLayout(0, 22));
        root.setBackground(LoginView.PAPER);
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setOpaque(false);
        List<WashingMachine> machines = controller.getMachines();
        refs = new CardRefs[machines.size()];
        for (int i = 0; i < machines.size(); i++) {
            gridPanel.add(makeCard(machines.get(i), i));
        }

        root.add(header(), BorderLayout.NORTH);
        root.add(gridPanel, BorderLayout.CENTER);
        setContentPane(root);
        controller.addListener(this);
        LoginView.maximize(this);
    }

    void enterRoom(String room) {
        roomNo = room;
        roomWho.setText("ห้องที่เข้าใช้ " + room);
        setVisible(true);
        toFront();
        LoginView.maximize(this);
    }

    void leaveRoom() {
        setVisible(false);
        login.showForNextRoom();
    }

    JComponent header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LoginView.LINE),
                BorderFactory.createEmptyBorder(8, 0, 18, 0)
        ));

        JLabel title = new JLabel("ตู้ซักผ้า");
        title.setFont(LoginView.uiFont(Font.BOLD, 32));
        title.setForeground(LoginView.INK);

        roomWho = new JLabel("ห้องที่เข้าใช้ " + roomNo);
        roomWho.setFont(LoginView.uiFont(Font.PLAIN, 14));
        roomWho.setForeground(LoginView.MUTED);
        roomWho.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton leave = LoginView.linkButton("ออกจากห้อง");
        leave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                leaveRoom();
            }
        });
        leave.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        roomWho.setAlignmentX(Component.LEFT_ALIGNMENT);
        leave.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);
        left.add(roomWho);
        left.add(leave);

        JLabel cap = new JLabel("เวลา");
        cap.setFont(LoginView.uiFont(Font.PLAIN, 12));
        cap.setForeground(LoginView.MUTED);
        cap.setAlignmentX(Component.RIGHT_ALIGNMENT);

        clockDash.setFont(LoginView.clockFont(34));
        clockDash.setForeground(LoginView.INK);
        clockDash.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel clockWrap = new JPanel();
        clockWrap.setOpaque(false);
        clockWrap.setLayout(new BoxLayout(clockWrap, BoxLayout.Y_AXIS));
        clockWrap.add(cap);
        clockWrap.add(clockDash);

        header.add(left, BorderLayout.WEST);
        header.add(clockWrap, BorderLayout.EAST);
        return header;
    }

    JPanel makeCard(final WashingMachine machine, int index) {
        Color fg = statusFg(machine.getStatus());
        Color bg = statusBg(machine.getStatus());

        LoginView.RoundPanel card = new LoginView.RoundPanel(18);
        card.setLayout(new GridBagLayout());
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(machine.getMachineId());
        name.setFont(LoginView.uiFont(Font.BOLD, 28));
        name.setForeground(LoginView.INK);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel kg = new JLabel("10 kg");
        kg.setFont(LoginView.uiFont(Font.PLAIN, 18));
        kg.setForeground(LoginView.MUTED);
        kg.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel namePanel = new JPanel();
        namePanel.setOpaque(false);
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        namePanel.add(name);
        namePanel.add(kg);

        JPanel idRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        idRow.setOpaque(false);
        idRow.add(new LoginView.WasherIcon(fg, 58));
        idRow.add(namePanel);
        idRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel chip = LoginView.badge(statusText(machine.getStatus()), fg, bg);
        JLabel roomLabel = line("ห้อง : " + roomText(machine), LoginView.INK);
        JLabel leftCaption = line(remainCaption(machine), LoginView.MUTED);
        JLabel leftLabel = new JLabel(formatRemain(machine.getTimeRemaining()));
        leftLabel.setFont(LoginView.clockFont(44));
        leftLabel.setForeground(LoginView.INK);
        leftLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel finishLabel = line(finishLine(machine), LoginView.INK);
        stack.add(idRow);
        stack.add(Box.createVerticalStrut(16));
        stack.add(chip);
        stack.add(Box.createVerticalStrut(18));
        stack.add(roomLabel);
        stack.add(Box.createVerticalStrut(10));
        stack.add(leftCaption);
        stack.add(leftLabel);
        stack.add(Box.createVerticalStrut(8));
        stack.add(finishLabel);
        JLabel hint = line("กดเพื่อเปิดจอเครื่องนี้", LoginView.HINT);
        hint.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        stack.add(hint);
        card.add(stack);

        refs[index] = new CardRefs(leftLabel, finishLabel);
        listen(card, machine.getMachineId());
        return card;
    }

    static JLabel line(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(LoginView.uiFont(Font.PLAIN, 20));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    void listen(Component component, final String machineId) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openMachine(machineId);
            }
        });
        if (component instanceof Container) {
            Component[] children = ((Container) component).getComponents();
            for (int i = 0; i < children.length; i++) {
                listen(children[i], machineId);
            }
        }
    }

    void openMachine(String machineId) {
        WashingMachine machine = controller.findMachine(machineId);
        if (machine == null) {
            return;
        }
        if (openView != null && openView.isDisplayable()) {
            return;
        }
        setVisible(false);
        openView = new MachineView(this, controller, machineId, roomNo);
        openView.setVisible(true);
    }

    void machineClosed() {
        if (openView == null) {
            return;
        }
        openView = null;
        setVisible(true);
        toFront();
        LoginView.maximize(this);
    }

    void replaceCard(WashingMachine machine) {
        int index = indexOf(machine.getMachineId());
        if (index < 0) {
            return;
        }
        gridPanel.remove(index);
        gridPanel.add(makeCard(machine, index), index);
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    int indexOf(String machineId) {
        List<WashingMachine> machines = controller.getMachines();
        for (int i = 0; i < machines.size(); i++) {
            if (machines.get(i).getMachineId().equals(machineId)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onStatusChanged(final WashingMachine machine) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                replaceCard(machine);
                if (openView != null && openView.isSameMachine(machine.getMachineId())) {
                    openView.showCurrent();
                }
            }
        });
    }

    @Override
    public void onTimerTick(final WashingMachine machine) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int index = indexOf(machine.getMachineId());
                if (index >= 0 && refs[index] != null) {
                    refs[index].left.setText(formatRemain(machine.getTimeRemaining()));
                    refs[index].finish.setText(finishLine(machine));
                }
                if (openView != null && openView.isSameMachine(machine.getMachineId())) {
                    openView.tick(machine);
                }
            }
        });
    }

    @Override
    public void onClockTick(final LocalTime now) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String text = now.format(CLOCK);
                clockDash.setText(text);
                if (openView != null) {
                    openView.setClock(text);
                }
            }
        });
    }

    static Color statusFg(MachineStatus status) {
        if (status == MachineStatus.IN_USE) {
            return LoginView.RED;
        }
        if (status == MachineStatus.PENDING) {
            return LoginView.YELLOW;
        }
        return LoginView.GREEN;
    }

    static Color statusBg(MachineStatus status) {
        if (status == MachineStatus.IN_USE) {
            return LoginView.RED_BG;
        }
        if (status == MachineStatus.PENDING) {
            return LoginView.YELLOW_BG;
        }
        return LoginView.GREEN_BG;
    }

    static String statusText(MachineStatus status) {
        if (status == MachineStatus.AVAILABLE) {
            return "ว่าง";
        }
        if (status == MachineStatus.IN_USE) {
            return "กำลังซัก";
        }
        return "รอเก็บผ้า";
    }

    static String roomText(WashingMachine machine) {
        String room = machine.getCurrentRoomNo();
        if (room == null || room.trim().isEmpty()) {
            return "--";
        }
        return room;
    }

    static String remainCaption(WashingMachine machine) {
        if (machine.getStatus() == MachineStatus.PENDING) {
            return "เหลือเวลาเก็บผ้า";
        }
        return "เหลือเวลา";
    }

    static String remainLine(WashingMachine machine) {
        if (machine.getStatus() == MachineStatus.PENDING) {
            return "เหลือเวลาเก็บผ้า  " + formatRemain(machine.getTimeRemaining());
        }
        if (machine.getStatus() == MachineStatus.IN_USE) {
            return "เหลือเวลา  " + formatRemain(machine.getTimeRemaining());
        }
        return "เหลือเวลา  --:--";
    }

    static String finishLine(WashingMachine machine) {
        if (machine.getStatus() == MachineStatus.IN_USE && machine.getExpectedFinish() != null) {
            return "ซักเสร็จประมาณ  " + machine.getExpectedFinish().format(HOUR);
        }
        if (machine.getStatus() == MachineStatus.PENDING) {
            return "ซักเสร็จแล้ว";
        }
        return "ซักเสร็จประมาณ  --:--";
    }

    static String formatRemain(int seconds) {
        if (seconds <= 0) {
            return "--:--";
        }
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    static final class CardRefs {
        final JLabel left;
        final JLabel finish;

        CardRefs(JLabel left, JLabel finish) {
            this.left = left;
            this.finish = finish;
        }
    }

    public static void main(String[] args) {
        LoginView.main(args);
    }
}
