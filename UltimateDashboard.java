package redmi;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

class Task implements Serializable {
    String name;
    boolean isCompleted;
    long timeSpent;
    Task(String name) { this.name = name; }
    @Override public String toString() { return name; } 

public class UltimateDashboard {
    private JFrame frame = new JFrame("FOCUS");
    private JTextField taskField = new JTextField();
    private DefaultListModel<Task> model = new DefaultListModel<>();
    private JList<Task> list = new JList<>(model);
    private ArrayList<Task> tasks;
    private Timer timer;
    private JProgressBar progressBar = new JProgressBar(0, 100);
    private final String FILE = "tasks.dat";

    private final Color BG = new Color(20, 20, 25), ACCENT = new Color(0, 200, 255);

    public UltimateDashboard() {
        tasks = loadTasks();
        tasks.forEach(model::addElement);

        JPanel side = new JPanel(new GridLayout(8, 1, 5, 5));
        side.setBackground(new Color(30, 30, 35));
        side.setBorder(new EmptyBorder(10, 10, 10, 10));
        String[] btns = {"▶ Start", "⏹ Stop", "✔ Done", "✖ Del", "📊 Stats", "🔔 Alert"};
        ActionListener[] actions = {e->startT(), e->stopT(), e->compT(), e->delT(), e->showC(), e->setR()};
        
        JLabel logo = new JLabel("Track It", 0);
        logo.setForeground(ACCENT);
        side.add(logo);
        for(int i=0; i<btns.length; i++) side.add(createBtn(btns[i], actions[i]));

        list.setBackground(BG);
        list.setForeground(Color.WHITE);
        list.setSelectionBackground(ACCENT);
        list.setCellRenderer(new TaskRenderer());

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        taskField.addActionListener(e -> addTask());
        progressBar.setStringPainted(true);

        main.add(taskField, BorderLayout.NORTH);
        main.add(new JScrollPane(list), BorderLayout.CENTER);
        main.add(progressBar, BorderLayout.SOUTH);

        frame.add(side, BorderLayout.WEST);
        frame.add(main, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(3);
        updateProgress();
        frame.setVisible(true);
    }

    private JButton createBtn(String t, ActionListener a) {
        JButton b = new JButton(t);
        b.addActionListener(a);
        return b;
    }

    private void addTask() {
        if (!taskField.getText().isEmpty()) {
            Task t = new Task(taskField.getText());
            tasks.add(t); model.addElement(t);
            taskField.setText(""); save(); updateProgress();
        }
    }

    private void compT() { 
        int i = list.getSelectedIndex();
        if (i != -1) { tasks.get(i).isCompleted = !tasks.get(i).isCompleted; list.repaint(); save(); updateProgress(); }
    }

    private void delT() {
        int i = list.getSelectedIndex();
        if (i != -1) { tasks.remove(i); model.remove(i); save(); updateProgress(); }
    }

    private void startT() {
        int i = list.getSelectedIndex();
        if (i == -1) return;
        if (timer != null && timer.isRunning()) return;
        timer = new Timer(1000, e -> { tasks.get(i).timeSpent++; list.repaint(); });
        timer.start();
    }

    private void stopT() { if (timer != null) { timer.stop(); save(); } }

    private void updateProgress() {
        if (tasks.isEmpty()) return;
        long done = tasks.stream().filter(t -> t.isCompleted).count();
        progressBar.setValue((int)((done * 100) / tasks.size()));
    }

    private void setR() {
        String s = JOptionPane.showInputDialog("Minutes:");
        if (s != null) new Timer(Integer.parseInt(s)*60000, e -> JOptionPane.showMessageDialog(null, "Alarm!")).start();
    }

    private void showC() {
        JOptionPane.showMessageDialog(null, "Completed: " + progressBar.getValue() + "%");
    }

    private void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))) { oos.writeObject(tasks); } catch (Exception e) {}
    }

    private ArrayList<Task> loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE))) { return (ArrayList<Task>) ois.readObject(); } 
        catch (Exception e) { return new ArrayList<>(); }
    }

    class TaskRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
            Task t = (Task) v;
            JLabel lbl = (JLabel) super.getListCellRendererComponent(l, v, i, s, f);
            String time = String.format(" [%02d:%02d]", t.timeSpent/60, t.timeSpent%60);
            lbl.setText((t.isCompleted ? "✔ " : "○ ") + t.name + time);
            lbl.setBorder(new EmptyBorder(10, 10, 10, 10));
            return lbl;
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(UltimateDashboard::new); }
}
