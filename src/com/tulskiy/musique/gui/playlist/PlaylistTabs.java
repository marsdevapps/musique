/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.gui.playlist;

import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.PlaylistManager;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 * Author: Denis Tulskiy
 * Date: Jun 21, 2010
 */
public class PlaylistTabs extends JPanel {
    private TableColumnModel columnModel;
    private ArrayList<PlaylistColumn> columns;

    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private PlaylistManager playlistManager = app.getPlaylistManager();
    private JTabbedPane tabbedPane;
    private Component singleTab;
    private boolean tabsVisible;

    private PlaylistTable selectedTable;

    private int dragTo = -1;
    private int dragFrom;
    private String singleTitle;

    public PlaylistTabs(ArrayList<PlaylistColumn> columns) {
        this.columns = columns;
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
        tabsVisible = true;

        tabbedPane.setFocusable(false);
        if (UIManager.getLookAndFeel().getName().contains("GTK")) {
            tabbedPane.setPreferredSize(new Dimension(10000, 30));
        } else {
            tabbedPane.setPreferredSize(new Dimension(10000, 25));
        }

        buildListeners();
        createPopupMenu();
    }

    private void checkTabCount() {
        if (!config.getBoolean("playlist.tabs.hideSingle", true)) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int count = tabbedPane.getTabCount();
                if (tabsVisible && count <= 1) {
                    tabsVisible = false;
                    remove(tabbedPane);

                    if (count == 1) {
                        singleTab = tabbedPane.getComponentAt(0);
                        singleTitle = tabbedPane.getTitleAt(0);
                        tabbedPane.removeAll();
                        add(singleTab, BorderLayout.CENTER);
                        setBorder(BorderFactory.createEtchedBorder());
                    }
                    revalidate();
                    repaint();
                } else if (!tabsVisible && count > 0) {
                    tabsVisible = true;

                    removeAll();
                    tabbedPane.add(singleTab, 0);
                    tabbedPane.setTitleAt(0, singleTitle);
                    singleTab = null;
                    add(tabbedPane, BorderLayout.CENTER);
                    setBorder(BorderFactory.createEmptyBorder());
                    revalidate();
                    repaint();
                }
            }
        });
    }

    @Override
    public void updateUI() {
        super.updateUI();

        if (tabbedPane != null)
            tabbedPane.updateUI();
    }

    public PlaylistTable getTableAt(int index) {
        try {
            Component comp;
            if (!tabsVisible) {
                comp = singleTab;
            } else {
                comp = tabbedPane.getComponentAt(index);
            }

            return (PlaylistTable) ((JScrollPane) comp).getViewport().getView();
        } catch (Exception e) {
            return null;
        }
    }

    public PlaylistTable getSelectedTable() {
        return selectedTable;
    }

    private void buildListeners() {
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index == -1)
                    return;
                selectedTable = getTableAt(index);
            }
        });

        tabbedPane.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragTo = tabbedPane.indexAtLocation(e.getX(), e.getY());
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
                    playlistManager.movePlaylist(dragFrom, dragTo);
                    Component comp = tabbedPane.getComponentAt(dragFrom);
                    String title = tabbedPane.getTitleAt(dragFrom);
                    tabbedPane.removeTabAt(dragFrom);
                    tabbedPane.insertTab(title, null, comp, null, dragTo);
                    tabbedPane.setSelectedIndex(dragTo);
                }
                dragTo = -1;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragFrom = tabbedPane.indexAtLocation(e.getX(), e.getY());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 &&
                    e.getClickCount() == 2) {
                    tabbedPane.getActionMap().get("newPlaylist").actionPerformed(
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                }
            }
        });
    }

    private void createPopupMenu() {
        ActionMap aMap = tabbedPane.getActionMap();

        aMap.put("newPlaylist", new AbstractAction("Add New Playlist") {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter Playlist Name", "New Playlist");
                addPlaylist(name);
            }
        });
        aMap.put("renamePlaylist", new AbstractAction("Rename") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedTable != null) {
                    Playlist playlist = selectedTable.getPlaylist();
                    String name = JOptionPane.showInputDialog("Rename", playlist.getName());
                    if (!Util.isEmpty(name)) {
                        playlist.setName(name);
                        tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), name);
                    }
                }
            }
        });
        aMap.put("removePlaylist", new AbstractAction("Remove Playlist") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = tabbedPane.getSelectedIndex();
                if (index != -1) {
                    Playlist playlist = selectedTable.getPlaylist();
                    tabbedPane.removeTabAt(index);
                    playlistManager.removePlaylist(playlist);
                    if (playlistManager.getTotalPlaylists() == 0) {
                        addPlaylist("Default");
                    }
                    checkTabCount();
                }
            }
        });
        aMap.put("savePlaylist", new AbstractAction("Save Playlist") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                String path = config.getString("playlist.lastDir", "");
                if (!path.isEmpty()) fc.setCurrentDirectory(new File(path));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileNameExtensionFilter("Musique Playlist", "mus"));
                Playlist playlist = selectedTable.getPlaylist();
                String fileName = playlist.getName().toLowerCase().replaceAll("\\s+", "_") + ".mus";
                fc.setSelectedFile(new File(fileName));

                int ret = fc.showSaveDialog(getParent());
                if (ret == JFileChooser.APPROVE_OPTION) {
                    playlist.save(fc.getSelectedFile());
                    config.setString("playlist.lastDir", fc.getCurrentDirectory().getAbsolutePath());
                }
            }
        });
        aMap.put("loadPlaylist", new AbstractAction("Load Playlist") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                String path = config.getString("playlist.lastDir", "");
                if (!path.isEmpty()) fc.setCurrentDirectory(new File(path));
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setFileFilter(new FileNameExtensionFilter("Musique Playlist", "mus"));

                int ret = fc.showOpenDialog(getParent());
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    Playlist playlist = addPlaylist(Util.removeExt(file.getName()));
                    playlist.load(file);
                    selectedTable.update();
                    config.setString("playlist.lastDir", fc.getCurrentDirectory().getAbsolutePath());
                }
            }
        });

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                show(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                show(e);
            }

            public void show(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = tabbedPane.indexAtLocation(e.getX(), e.getY());
                    if (index != -1)
                        tabbedPane.setSelectedIndex(index);
                    buildPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private JPopupMenu buildPopupMenu() {
        ActionMap aMap = tabbedPane.getActionMap();
        final JPopupMenu tabMenu = new JPopupMenu();
        ImageIcon emptyIcon = new ImageIcon(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));
        tabMenu.add(aMap.get("newPlaylist")).setIcon(emptyIcon);
        tabMenu.add(aMap.get("renamePlaylist"));
        tabMenu.add(aMap.get("removePlaylist"));
        tabMenu.addSeparator();
        tabMenu.add(aMap.get("savePlaylist"));
        tabMenu.add(aMap.get("loadPlaylist"));
        Util.fixIconTextGap(tabMenu);
        return tabMenu;
    }

    public Playlist addPlaylist(String name) {
        if (!Util.isEmpty(name)) {
            Playlist playlist = playlistManager.addPlaylist(name);
            addPlaylist(playlist);
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            return playlist;
        }
        return null;
    }

    public void addPlaylist(Playlist playlist) {
        PlaylistTable newTable = new PlaylistTable(playlist, columns);
        newTable.setUpDndCCP();
        if (columnModel == null) {
            columnModel = newTable.getColumnModel();
        } else {
            newTable.setColumnModel(columnModel);
        }
        tabbedPane.add(playlist.getName(), newTable.getScrollPane());
        checkTabCount();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (dragTo != -1 && dragFrom != -1 && dragTo != dragFrom) {
            Rectangle b = tabbedPane.getBoundsAt(dragTo);
            g.setColor(Color.GRAY);
            int x = (int) (b.getX());
            if (dragTo > dragFrom)
                x += b.getWidth();
            int y = (int) b.getY();
            g.fillRect(x - 1, y + 2, 3, (int) b.getHeight() - 2);

            int[] xP = {x - 1, x - 5, x + 5, x + 1};
            int[] yP = {y + 2, y - 5, y - 5, y + 2};
            g.fillPolygon(xP, yP, xP.length);
        }
    }

    public void setSelectedIndex(int i) {
        if (tabbedPane.getTabCount() < i)
            tabbedPane.setSelectedIndex(i);
    }

    public int getTabCount() {
        if (!tabsVisible) {
            return singleTab != null ? 1 : 0;
        } else {
            return tabbedPane.getTabCount();
        }
    }

    public void addTab(String name, JScrollPane comp) {
        tabbedPane.addTab(name, comp);
        checkTabCount();
    }

    public ActionMap getActions() {
        return tabbedPane.getActionMap();
    }
}
