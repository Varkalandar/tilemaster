/*
 * File: TilesetEditor.java
 * Creation: 2010/05/25
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor;

import asktools.ColorPalette;
import asktools.ColorPaletteInterface;
import asktools.Requester;
import itemizer.editor.ui.*;
import itemizer.item.ItemConfiguration;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import tilemaster.editor.colors.PaletteEditorFrame;
import tilemaster.editor.paintingtools.*;
import tilemaster.editor.paintingtools.Fillmachine.Filler;
import tilemaster.editor.paintingtools.filler.*;
import tilemaster.editor.transforms.*;
import tilemaster.editor.ui.AnimationPreview;
import tilemaster.editor.ui.ImageView;
import tilemaster.editor.ui.PreviewFrame;
import tilemaster.editor.ui.TileCellRenderer;
import tilemaster.file.FileSelector;
import tilemaster.file.FileWrapper;
import tilemaster.io.IOPluginBroker;
import tilemaster.tile.IdPool;
import tilemaster.tile.TileDescriptor;
import tilemaster.tile.TileSet;

/**
 * Tilemaster tile set editor.
 * 
 * @author Hj. Malthaner
 */
public class TilesetEditor
        extends JFrame
        implements asktools.ColorPaletteInterface
{
    private static final Logger LOGGER = Logger.getLogger(TilesetEditor.class.getName());
    
    private static final String VERSION = "v0.39";
    private static final String DATE = "(2020/03/12)";
    
    private File currentTileSetFile = null;
    private File currentImportFile = null;
    private File currentSingleImportFile = null;

    private IntPanel intPanel;
    private StringPanel stringPanel;
    private final Requester requester;
    private final ImageView imageView;
    private final JScrollPane imageViewScrollPane;
    private TileSet tileSet;
    
    private int currentTile = -1;
    
    private int zoomLevel = 1;
    
    private BufferedImage canvas = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
    private BufferedImage undoCanvas = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
    
    /**
     * Import must be en/disabled depending on the situation, so it must be global
     */
    private JMenuItem importMenuItem;
    private final ColorPalette colorPalette;
    private final Selection selection;
    private int selectedTab;
    public final Brush brush;
    
    private PreviewFrame previewFrame;
    
    /**
     * Used when importing tiles from other sets
     */
    private TileDescriptor importedTile;

    /**
     * Used as background in imageView.
     */
    private TileDescriptor backgroundTile;
    private TileSet backgroundTileSet;

    /**
     * Used to speed up canvas clearing.
     */
    private int [] clearPixels = new int [0];

    /**
     * Customized file chooser.
     */
    private final FileSelector fs;

    /** 
     * for borderblend tool 
     */
    private int blendAmount = 50;
    
    
    private final JToggleButton btHiddenExtra = new JToggleButton();
    private String animationString = "0, 8, 100";
    
    /** 
     * Creates new form TilesetEditor
     */
    public TilesetEditor()
    {
        try {
            for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }        

        fs = new FileSelector();
        requester = new Requester();
        
        initComponents();
        toolButtonGroup.add(btHiddenExtra);
        
        createMenuBar();

        rasterField.setDocument(new FilteredDocument("0123456789x"));
        previewFrame = new PreviewFrame();

        URL imgURL = getClass().getResource("/tilemaster/editor/mastertile.png");
        ImageIcon icon = new ImageIcon(imgURL, "mastertile");
        setIconImage(icon.getImage());

        imageList.setCellRenderer(new TileCellRenderer());
        imageList.setModel(new DefaultListModel());
        imageList.requestFocusInWindow();

        // shared between selection and image view
        Rectangle selectionRectangle = new Rectangle();

        selection = new Selection(selectionRectangle);
        selection.clear();

        brush = new Brush();
        
        imageView = new ImageView(selectionRectangle);
        imageView.setBackground(Color.DARK_GRAY);
        imageView.setImage(canvas);
        imageViewScrollPane = new JScrollPane(imageView);
        imagePanelContainer.add(imageViewScrollPane);
        imageView.addMouseListener(new MouseEventHandler());
        imageView.addMouseMotionListener(new MouseMotionHandler());
        imageView.addMouseWheelListener(new MouseWheelHandler());

        zoomLevel = 5;
        imageView.setZoomLevel(zoomLevel);

        colorPalette = new ColorPalette(this, 24);
        JScrollPane colorScroller = new JScrollPane(colorPalette);
        colorScroller.setBorder(null);
        colorScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        colorsPanelContainer.add(colorScroller);

        setTitle("Tilemaster " + VERSION);
        setSize(960, 732);

        btPoint.setSelected(true);

        newTileSet(20, getDefaultTileConfiguration());

        restoreWindowState();

        ToolPluginBroker.loadPlugins(toolPluginList);
        
        setPaintingTool(new PlotTool());
    }

    public Selection getSelection()
    {
        return selection;
    }

    public ColorPalette getColorPalette()
    {
        return colorPalette;
    }
    
    public TileSet getTileSet()
    {
        return tileSet;
    }
    
    private ItemConfiguration getDefaultTileConfiguration()
    {
        ItemConfiguration tileConfiguration = new ItemConfiguration();

        tileConfiguration.stringLabels =
                new String [] {"Name", "Tags"};
        tileConfiguration.intLabels =
                new String [] {"Value"};

        tileConfiguration.tripletLabels = new String [0];

        return tileConfiguration;
    }


    private void createMenuBar()
    {
        JMenuItem newTileSet = new JMenuItem("New");
        fileMenu.add(newTileSet);
        newTileSet.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    askNewTileSet(tileSet != null ? tileSet.size() : 20,
                                  getDefaultTileConfiguration());
                }
            }
        );
        newTileSet.setMnemonic(KeyEvent.VK_N);
        newTileSet.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));

        JMenuItem open = new JMenuItem("Open");
        fileMenu.add(open);
        open.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    readTileSet();
                }
            }
        );
        open.setMnemonic(KeyEvent.VK_O);
        open.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
        
        JMenuItem save = new JMenuItem("Save");
        fileMenu.add(save);
        save.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    writeTileSet();
                }
            }
        );
        save.setMnemonic(KeyEvent.VK_S);
        save.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
        
        JMenuItem saveAs = new JMenuItem("Save As");
        fileMenu.add(saveAs);
        saveAs.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    writetileSetAs();
                }
            }
        );

        JMenuItem resize = new JMenuItem("Resize Set");
        fileMenu.add(resize);
        resize.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    resizeTileset();
                }
            }
        );
        
        fileMenu.addSeparator();

        JMenuItem lcp = new JMenuItem("Load Color Palette");
        fileMenu.add(lcp);
        lcp.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    selectColorMapForLoading();
                }
            }
        );
        JMenuItem scp = new JMenuItem("Save Color Palette");
        fileMenu.add(scp);
        scp.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    selectColorMapForSaving();
                }
            }
        );

        fileMenu.addSeparator();

        importMenuItem = new JMenuItem("Import Image As Tile");
        fileMenu.add(importMenuItem);
        importMenuItem.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    importSingleImage();
                }
            }
        );
        importMenuItem.setMnemonic(KeyEvent.VK_M);
        importMenuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl M"));

        JMenuItem importSequence = new JMenuItem("Import Image Sequence");
        fileMenu.add(importSequence);
        importSequence.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    importImageSequence();
                }
            }
        );
        
        JMenuItem mimp = new JMenuItem("Import Tiles From Image");
        fileMenu.add(mimp);
        mimp.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    // importMultiImage();
                    importTilesFromImage();
                }
            }
        );

        JMenuItem importList = new JMenuItem("Import From Tile Set");
        fileMenu.add(importList);
        importList.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    importFromTileSet();
                }
            }
        );
        importList.setAccelerator(KeyStroke.getKeyStroke("ctrl I"));

        fileMenu.addSeparator();

        JMenuItem exportARGB = new JMenuItem("Export PNG (ARGB)");
        fileMenu.add(exportARGB);
        exportARGB.addActionListener(
            new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    exportSingleImage(true);
                }
            }
        );
        exportARGB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
        
        JMenuItem exportRGB = new JMenuItem("Export PNG (RGB)");
        fileMenu.add(exportRGB);
        exportRGB.addActionListener(
            new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    exportSingleImage(false);
                }
            }
        );
        exportRGB.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));

        JMenuItem exportGif = new JMenuItem("Export GIF (8 bit)");
        fileMenu.add(exportGif);
        exportGif.addActionListener(
            new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    exportSingleImageGif();
                }
            }
        );
        exportGif.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, KeyEvent.CTRL_DOWN_MASK));

        fileMenu.addSeparator();

        JMenuItem exit = new JMenuItem("Quit");
        fileMenu.add(exit);
        exit.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    saveWindowState();
                    System.exit(0);
                }
            }
        );
        exit.setMnemonic(KeyEvent.VK_Q);
        exit.setAccelerator(KeyStroke.getKeyStroke("ctrl Q"));

        JMenuItem undo = new JMenuItem("Undo");
        editMenu.add(undo);
        undo.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    undo();
                    imageClicked(currentTile);
                }
            }
        );
        undo.setAccelerator(KeyStroke.getKeyStroke("ctrl Z"));

        JMenuItem paste = new JMenuItem("Paste");
        editMenu.add(paste);
        paste.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    SystemClipboard.paste(TilesetEditor.this);
                }
            }
        );
        paste.setAccelerator(KeyStroke.getKeyStroke("ctrl V"));

        JMenuItem copy = new JMenuItem("Copy");
        editMenu.add(copy);
        copy.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    copyToClipboard();
                }
            }
        );
        copy.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));

        JMenuItem setCanvasSizeItem = new JMenuItem("Set Canvas Size");
        editMenu.add(setCanvasSizeItem);
        setCanvasSizeItem.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    String sizeString = 
                        requester.askString(TilesetEditor.this,
                                "Please enter the new canvas size:",
                                "" + canvas.getWidth() + "x" + canvas.getHeight());

                    String [] parts = sizeString.split("x");
                    if(parts.length == 2)
                    {
                        final int width = Integer.parseInt(parts[0]);
                        final int height = Integer.parseInt(parts[1]);

                        setCanvasSize(width, height);
                    }
                }
            }
        );
        setCanvasSizeItem.setAccelerator(KeyStroke.getKeyStroke("F2"));

        editMenu.addSeparator();

        JMenuItem zoomIn = new JMenuItem("Zoom In");
        editMenu.add(zoomIn);
        zoomIn.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    // zoomLevel *= 2;
                    zoomLevel += 1;
                    imageView.setZoomLevel(zoomLevel);
                }
            }
        );
        zoomIn.setAccelerator(KeyStroke.getKeyStroke("ctrl PLUS"));

        JMenuItem zoomOut = new JMenuItem("Zoom Out");
        editMenu.add(zoomOut);
        zoomOut.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(zoomLevel > 1) {
                        // zoomLevel /= 2;
                        zoomLevel -= 1;
                    }
                    imageView.setZoomLevel(zoomLevel);
                }
            }
        );
        zoomOut.setAccelerator(KeyStroke.getKeyStroke("ctrl MINUS"));

        editMenu.addSeparator();

        JMenuItem flipH = new JMenuItem("Flip Selection Horiz.");
        editMenu.add(flipH);
        flipH.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    selection.flipHoriz();
                    imageView.repaint();
                }
            }
        );

        JMenuItem flipV = new JMenuItem("Flip Selection Vert.");
        editMenu.add(flipV);
        flipV.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    selection.flipVert();
                    imageView.repaint();
                }
            }
        );
        
        editMenu.addSeparator();
    
        JMenuItem panLeft = new JMenuItem("Pan Left");
        editMenu.add(panLeft);
        panLeft.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(currentTile != -1) {
                        pan(-1, 0);
                    }
                }
            }
        );
        panLeft.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK));

        JMenuItem panRight = new JMenuItem("Pan Right");
        editMenu.add(panRight);
        panRight.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(currentTile != -1) {
                        pan(1, 0);
                    }
                }
            }
        );
        panRight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK));

        JMenuItem panUp = new JMenuItem("Pan Up");
        editMenu.add(panUp);
        panUp.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(currentTile != -1) {
                        pan(0, -1);
                    }
                }
            }
        );
        panUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK));

        JMenuItem panDown = new JMenuItem("Pan Down");
        editMenu.add(panDown);
        panDown.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(currentTile != -1) {
                        pan(0, 1);
                    }
                }
            }
        );
        panDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK));

        editMenu.addSeparator();

        JMenuItem insnew = new JMenuItem("Insert New Tile");
        editMenu.add(insnew);
        insnew.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(imageList.getSelectedIndex() > -1 && tileSet != null) {
                        insertNewTileAt(imageList.getSelectedIndex());
                    }
                }
            }
        );
        insnew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK));

        JMenuItem remcur = new JMenuItem("Remove Selected Tile");
        editMenu.add(remcur);
        remcur.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(imageList.getSelectedIndex() > -1 && tileSet != null) {
                        removeTileFrom(imageList.getSelectedIndex());
                    }
                }
            }
        );
        remcur.setAccelerator(KeyStroke.getKeyStroke("DELETE"));

        JMenuItem copyfromtile = new JMenuItem("Copy From Tile");
        editMenu.add(copyfromtile);
        copyfromtile.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(imageList.getSelectedIndex() > -1 && tileSet != null) {
                        copyFromTile();
                    }
                }
            }
        );

        editMenu.addSeparator();

        JMenuItem lobai = new JMenuItem("Load Background Image");
        editMenu.add(lobai);
        lobai.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    loadBackgroundImage();
                }
            }
        );

        JMenuItem selu = new JMenuItem("Load Background Tile");
        editMenu.add(selu);
        selu.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    loadBackgroundTile();
                }
            }
        );

        JMenuItem loadBackgroundTileset = new JMenuItem("Load Background Tileset");
        editMenu.add(loadBackgroundTileset);
        loadBackgroundTileset.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) 
            {
                    loadBackgroundTileSet();
                }
            }
        );

        JMenuItem cleba = new JMenuItem("Clear Background");
        editMenu.add(cleba);
        cleba.addActionListener(
            new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) 
            {
                    backgroundTileSet = null;
                    backgroundTile = null;
                    imageView.setUnderlay(null);
                }
            }
        );
        
        JMenuItem sebacol = new JMenuItem("Set Background Color");
        editMenu.add(sebacol);
        sebacol.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) 
                {
                    imageView.setBackground(paintColor);
                }
            }
        );

        JMenuItem shrink2 = new JMenuItem("Quick Resize");
        transformsMenu.add(shrink2);
        shrink2.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(currentTile != -1) 
                    {
                        askResize(false);
                    }
                }
            }
        );
        // shrink2.setEnabled(false);

        JMenuItem shrink3 = new JMenuItem("Smooth Resize");
        transformsMenu.add(shrink3);
        shrink3.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(currentTile != -1)
                    {
                        askResize(true);
                    }
                }
            }
        );

        JMenuItem quickReshape = new JMenuItem("Quick Reshape");
        transformsMenu.add(quickReshape);
        quickReshape.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(currentTile != -1)
                    {
                       askReshape(false);
                    }
                }
            }
        );

        JMenuItem smoothReshape = new JMenuItem("Smooth Reshape");
        transformsMenu.add(smoothReshape);
        smoothReshape.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e) {
                    if(currentTile != -1)
                    {
                       askReshape(true);
                    }
                }
            }
        );

        JMenuItem cropTile = new JMenuItem("Crop Tile");
        transformsMenu.add(cropTile);
        cropTile.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                   callTransform(new TransformCropTile());
                }
            }
        );
        cropTile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_DOWN_MASK));

        JMenuItem clearTile = new JMenuItem("Clear Tile");
        transformsMenu.add(clearTile);
        clearTile.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                   clearImage(canvas);
                   imageClicked(currentTile);
                }
            }
        );
        clearTile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.ALT_DOWN_MASK));

        transformsMenu.addSeparator();

        JMenuItem toColormap = new JMenuItem("Reduce to Colormap");
        transformsMenu.add(toColormap);
        toColormap.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    callTransform(new ReduceToColormap());
                }
            }
        );

        JMenuItem toColormapED = new JMenuItem("Reduce to Colormap ED");
        transformsMenu.add(toColormapED);
        toColormapED.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    callTransform(new ReduceToColormapED());
                }
            }
        );
        toColormapED.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK));


        JMenuItem tiledTiles = new JMenuItem("Rectangular Tiles");
        previewMenu.add(tiledTiles);
        tiledTiles.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    showTiledPreview();
                }
            }
        );
        tiledTiles.setAccelerator(KeyStroke.getKeyStroke("ctrl COMMA"));

        JMenuItem isoTiles = new JMenuItem("Isometric Tiles");
        previewMenu.add(isoTiles);
        isoTiles.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    showIsoPreview();
                }
            }
        );
        isoTiles.setAccelerator(KeyStroke.getKeyStroke("ctrl PERIOD"));

        JMenuItem animation = new JMenuItem("Animation");
        previewMenu.add(animation);
        animation.addActionListener(
            new ActionListener() 
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    showAnimation();
                }
            }
        );
        animation.setAccelerator(KeyStroke.getKeyStroke("alt MINUS"));

        JMenuItem about = new JMenuItem("About Tilemaster");
        helpMenu.add(about);
        about.addActionListener(
            new ActionListener() 
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    about();
                }
            }
        );
        about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
    }

    private void callTransform(final TileTransform tileTransform)
    {
        if(currentTile != -1)
        {
            final boolean ok = tileTransform.askUserData(this);
            if(ok)
            {
                final BufferedImage img = tileTransform.transform(canvas);

                if(img != canvas)
                {
                    tileSet.get(currentTile).img = img;
                    clearImage(canvas);
                    updateCanvas(img);
                }

                updateImageData(currentTile);
                imageView.repaint();
            }
        }
    }
    
    private void about()
    {
        JOptionPane.showMessageDialog(this,
                "<html><h3>Tilemaster - Tile Set Editor " + VERSION + " " + DATE + "</h3>" +
                "<p>" +
                "Author: Hj. Malthaner<br>" +
                "Email: h_malthaner@users.sourceforge.net<br>" +
                "Web: https://github.com/Varkalandar/Tilemaster/" +
                "</p></html>",
                "About Tilemaster Tile Set Editor",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private String askAttributeName(String input)
    {
        return requester.askString(this,
                "Please enter the name of the new attribute:",
                input);
    }

    private Dimension askRasterSize(String preset)
    {
        final Dimension result = requester.askDimension(this,
                "<html>Please enter the tile raster to use,<br>" +
                "in format WxH:</html>", preset);

        return result;
    }

    private void initTileDataPanels()
    {
        final ItemConfiguration tileConfiguration = tileSet.getTileConfiguration();
        intPanel = new IntPanel(tileConfiguration, true);
        stringPanel = new StringPanel(tileConfiguration, true);

        intScroller.setViewportView(intPanel);
        stringScroller.setViewportView(stringPanel);

        for(int i=0; i<tileConfiguration.intLabels.length; i++)
        {
            final int n = i;
            intPanel.editButtons[n].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final String name = askAttributeName(tileConfiguration.intLabels[n]);
                    tileConfiguration.intLabels[n] = name;
                    intPanel.labels[n].setText(" " + name + ":  ");
                }
            });
            intPanel.deleteButtons[n].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tileConfiguration.removeIntAttribute(n);
                    initTileDataPanels();
                }
            });
        }

        for(int i=0; i<tileConfiguration.stringLabels.length; i++)
        {
            final int n = i;
            stringPanel.editButtons[n].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final String name = askAttributeName(tileConfiguration.stringLabels[n]);
                    tileConfiguration.stringLabels[n] = name;
                    stringPanel.labels[n].setText(" " + name + ":  ");
                }
            });
            stringPanel.deleteButtons[n].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tileConfiguration.removeStringAttribute(n);
                    initTileDataPanels();
                }
            });
        }

        intPanel.addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTileData(currentTile);

                tileSet.getTileConfiguration()
                       .addNewIntAttribute(askAttributeName(""));
                initTileDataPanels();
                showTileAttributes(currentTile);
            }
        });
        stringPanel.addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTileData(currentTile);

                tileSet.getTileConfiguration()
                       .addNewStringAttribute(askAttributeName(""));
                initTileDataPanels();
                showTileAttributes(currentTile);
            }
        });
    }

    /**
     * Creates a new tileset
     * @author Hj. Malthaner
     */
    private void askNewTileSet(int preset,
                               ItemConfiguration config)
    {
        boolean ok = false;
        
        do
        {
            String numberString = 
                requester.askString(this, 
                    "Please enter the number of tiles for the new set:", "" + preset);

            try
            {
                int n = Integer.parseInt(numberString);

                if(n > 0)
                {
                    ok = true;
                    newTileSet(n, config);
                }
            }
            catch (NumberFormatException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        while (!ok);
    }

    private void newTileSet(final int size,
                            ItemConfiguration config)
    {
        final Cursor cursor = getCursor();

        try
        {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            tileSet = new TileSet(config, size);

            initTileDataPanels();
            clearImage(canvas);

            for(int i=0; i<size; i++)
            {
                updateTileData(i);
            }

            updateForNewImageList();
            imageList.setSelectedIndex(0);

            currentTileSetFile = null;
            setTitle("Tilemaster " + VERSION);
        }
        finally
        {
            setCursor(cursor);
        }
    }
    
    /**
     * Creates a new tileset with user defined size and copies as many 
     * tiles from the old set into the new set as possible.
     * @author Hj. Malthaner
     */
    private void resizeTileset()
    {
        TileSet old = tileSet;
        
        askNewTileSet(tileSet != null ? tileSet.size() : 20, 
                      old.getTileConfiguration());
        
        final int end = Math.min(old.size(), tileSet.size());
        
        final IdPool pool = new IdPool();

        for(int i=0; i<end; i++) {
            tileSet.set(i, old.get(i));
            pool.add(tileSet.get(i).tileId);
        }

        for(int i=end; i<tileSet.size(); i++) 
        {
            tileSet.get(i).tileId = pool.allocateNextId();
        }
        
        updateForNewImageList();
        repaint(100);        
    }

    private void insertNewTileAt(int index)
    {
        TileDescriptor tld = tileSet.createTileDescriptor();
        tld.img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        tileSet.insert(index, tld);
        updateForNewImageList();
        imageList.setSelectedIndex(index);
    }
    
    private void removeTileFrom(int index)
    {
        tileSet.remove(index);
        updateForNewImageList();
        imageList.setSelectedIndex(index);
    }

    private void copyFromTile()
    {
        final int tileId = 
            requester.askNumber(this, "Copy from which tile id?", 1);
        
        final int tileNo = tileSet.numberFromId(tileId);
        
        importImage(tileSet.get(tileNo).img);
    }

    private TileSet askReadTileSet(boolean setCurrentSet)
    {
        TileSet newSet = null;
    
        try
        {
            String filterList = IOPluginBroker.getFileFilterList("|");
            File result;
            result = fs.selectFile(tilesetSplitter, "Please select the file to load:",
                                   filterList, "Image list files",
                                   currentTileSetFile, FileSelector.Mode.OPEN);

            if(result != null)
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                
                if(setCurrentSet)
                {
                    currentTileSetFile = result;
                }
                
                newSet = IOPluginBroker.read(result.getAbsolutePath());
            }
        }  
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally
        {
            setCursor(Cursor.getDefaultCursor());
        }
        
        return newSet;
    }
    
    /**
     * Read a tile Set from a file or directory.
     */
    public void readTileSet()
    {
        try
        {
            TileSet newSet = askReadTileSet(true);
            
            if(newSet != null)
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                tileSet = newSet;
                
                // Hajo: some Tilemaster versions created sets
                // with duplicate IDs. We try to repair such sets here.
                
                IdPool pool = new IdPool();
                ArrayList <Integer> duplicates = new ArrayList<Integer> (1024);
                
                // 1) gather duplicate IDs
                for(int i=0; i<tileSet.size(); i++) 
                {
                    TileDescriptor tld = tileSet.get(i);
                    int id = tld.tileId;
                    
                    if(pool.contains(id))
                    {
                        duplicates.add(i);
                    }
                    else
                    {
                        pool.add(id);
                    }
                }

                for(int i : duplicates)
                {
                    TileDescriptor tld = tileSet.get(i);
                    tld.tileId = pool.allocateNextId();
                }
                
                if(!duplicates.isEmpty())
                {
                    requester.askChoice(this, "This tile set has duplicate IDs.", "Repair IDs");
                }
                
                
                updateForNewImageList();
                
                setTitle("Tilemaster - " + currentTileSetFile);
            }        
        }  
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally
        {
            setCursor(Cursor.getDefaultCursor());
        }
    }


    private void addTileIds(TileSet tileSet)
    {
        final IdPool pool = new IdPool();

        for(int i=0; i<tileSet.size(); i++) 
        {
            pool.add(tileSet.get(i).tileId);
        }

        for(int i=0; i<tileSet.size(); i++) 
        {
            final TileDescriptor tld = tileSet.get(i);

            // Give tiles which have no id yet
            // an unique id.
            if(tld.tileId == 0) 
            {
                tld.tileId = pool.allocateNextId();
            }
        }
    }

    public void updateImageViewOverlayOffset(int x, int y)
    {
        imageView.setOverlayOffset(x, y);
        imageView.repaint(100);
    }
        
    public void updateImageViewOverlay(BufferedImage img)
    {
        imageView.setOverlay(img);
    }

    public void updateImageViewSelection(int x1, int y1, int x2, int y2)
    {
        imageView.setSelectionArea(x1, y1, x2, y2);
    }
    
    /**
     * After loading or creating a new tilesest, this will update the view.
     */
    private void updateForNewImageList()
    {
        rasterField.setText("" + tileSet.rasterX +"x" + tileSet.rasterY);
        addTileIds(tileSet);
        
        currentTile = -1;

        initTileDataPanels();
        DefaultListModel model = new DefaultListModel();

        for(int i=0; i<tileSet.size(); i++) 
        {
            model.addElement(tileSet.get(i));
        }

        imageList.setModel(model);
        imageList.setSelectedIndex(0);
    }

    private void updateTileSetRaster()
    {
        final String text = rasterField.getText();
        final String [] parts = text.split("x");

        if(parts.length == 2)
        {
            tileSet.rasterX = Integer.parseInt(parts[0]);
            tileSet.rasterY = Integer.parseInt(parts[1]);
        }
        else
        {
            final Dimension dim = askRasterSize("32x32");
            rasterField.setText("" + dim.width + "x" + dim.height);
        }
    }
    
    /**
     * Read a tile list and let the user select an image to import
     */
    private void importFromTileSetAux(final Action action)
    {
        try 
        {
            String filterList = IOPluginBroker.getFileFilterList("|");
            File result;
            result = fs.selectFile(tilesetSplitter, "Please select the tile set to import from:",
                                   filterList, "Image list files",
                                   currentImportFile, FileSelector.Mode.OPEN);

            importedTile = null;

            if(result != null)
            {
                // Hajo: remember path for next import
                currentImportFile = result;
                
                // Hajo: now read the tileSet and display the selector
                final TileSet importedtileSet = IOPluginBroker.read(result.getPath());

                DefaultListModel model = new DefaultListModel();

                for(int i=0; i<importedtileSet.size(); i++)
                {
                    model.addElement(importedtileSet.get(i));
                }

                final JList selectorList = new JList();
                selectorList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
                selectorList.setCellRenderer(new TileCellRenderer());
                selectorList.setModel(model);
                selectorList.setVisibleRowCount(-1);
                        
                final JFrame selFrame = new JFrame("Select from list to import:");

                selectorList.addListSelectionListener(new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        int selection = selectorList.getSelectedIndex();

                        if(selection >= 0 && selection < importedtileSet.size() &&
                           currentTile >= 0 && currentTile < tileSet.size()) 
                        {
                            importedTile = importedtileSet.get(selection);

                            action.actionPerformed(null);

                            // More actions possible?
                            if(action.isEnabled() == false)
                            {
                                selFrame.dispose();
                            }
                        }
                    }
                });
                
                selFrame.getRootPane().setLayout(new BorderLayout());
                selFrame.getRootPane().add(new JScrollPane(selectorList));
                selFrame.setSize(640, 300);
                selFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                selFrame.setVisible(true);
            }
        
        }
        catch (HeadlessException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Read a tile list and let the user select an image to import
     */
    private void importFromTileSet()
    {
        importFromTileSetAux(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Give imported tile the proper ID for this tile slot
                // to avoid double IDs in the set
                
                final int id = tileSet.get(currentTile).tileId;
                importedTile.tileId = id;

                tileSet.set(currentTile, importedTile);

                final int image = currentTile;
                currentTile = -1;

                TilesetEditor.this.imageClicked(image);
                System.err.println("Import: replaced image " + image + " with imported image.");
                refreshListEntry(image);
                setEnabled(true);
            }
        });
    }

    private void updateUnderlayOffset()
    {
        final int index = imageList.getSelectedIndex();

        if(index > -1)
        {
            final TileDescriptor tld = tileSet.get(index);
            offsetField.setText("" + tld.offX + ", " + tld.offY);
            
            if(backgroundTile != null)
            {
                imageView.setUnderlayOffset(backgroundTile.offX-tld.offX,
                                            backgroundTile.offY-tld.offY);
            }
        }
    }
    
    private void setFootPoint(int x, int y)
    {
        final int index = imageList.getSelectedIndex();

        if(index > -1)
        {
            final TileDescriptor tld = tileSet.get(index);
            tld.footX = x;
            tld.footY = y;
            footField.setText("" + tld.footX + ", " + tld.footY);
        }
    }
    
    private void loadBackgroundImage()
    {
        Image img = selectImageFile();

        if(img != null)
        {
            ItemConfiguration config = tileSet.getTileConfiguration();

            final String [] strings = stringPanel.getStringValues();
            final int [] ints = intPanel.getIntValues();

            backgroundTile = new TileDescriptor(config, strings, ints);
            
            BufferedImage bim = new BufferedImage(img.getWidth(null),
                                                  img.getHeight(null),
                                                  BufferedImage.TYPE_INT_ARGB);
            bim.getGraphics().drawImage(img, 0, 0, null);
            backgroundTile.img = bim;
            imageView.setUnderlay(bim);
            updateUnderlayOffset();
        }
    }


    private void loadBackgroundTile()
    {
        importFromTileSetAux(new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                backgroundTile = importedTile;
                imageView.setUnderlay(backgroundTile.img);
                updateUnderlayOffset();
                setEnabled(false);
            }
        });
    }

    private void loadBackgroundTileSet()
    {
        TileSet newSet = askReadTileSet(false);
        
        backgroundTileSet = newSet;
        if(backgroundTileSet != null)
        {
            backgroundTile = backgroundTileSet.get(currentTile);
            imageView.setUnderlay(backgroundTile.img);
            updateUnderlayOffset();
        }
        else
        {
            backgroundTile = null;
            imageView.setUnderlay(null);
        }
    }

    public void writetileSetAs()
    {
        try
        {
            File result;
            String filterList = IOPluginBroker.getFileFilterList("|");
            result = fs.selectFile(tilesetSplitter, "Saving tile set:", filterList,
                                   "Image list files",
                                   null, FileSelector.Mode.WRITE);


            if(result != null)
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                currentTileSetFile = result;
                writeTileSet();

                setTitle("Tilemaster - " + currentTileSetFile);
            }
            else
            {
                // anything to do?
            }
        
        }  
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally
        {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    
    /**
     * Save the tileSet to the currently selected image list file
     *
     * @author Hj. Malthaner
     */
    private void writeTileSet() 
    {
        if(currentTileSetFile == null)
        {
            // Hajo: no file/name yet, ask for file
            // This will call us again after selecting a file!
            writetileSetAs();
        }
        else
        {
            try
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                // Hajo: convert formerly edited image
                updateTileData(currentTile);
                updateTileSetRaster();

                IOPluginBroker.write(currentTileSetFile.getAbsolutePath(),
                                     tileSet);
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            finally
            {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private File selectColorMapFile(String title, FileSelector.Mode mode)
    {
        File result = null;

        try
        {
            result = fs.selectFile(tilesetSplitter, title, ".pal", "Palette files", null, mode);
        }
        catch (Exception ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return result;
    }

    private void selectColorMapForLoading()
    {
        File file = selectColorMapFile("Select a color palette to load:",
                                       FileSelector.Mode.OPEN);
        if(file != null)
        {
            loadColorMap(file);
        }
    }

    private void selectColorMapForSaving()
    {
        File file = selectColorMapFile("Select a color palette to save:",
                                       FileSelector.Mode.WRITE);
        if(file != null)
        {
            saveColorMap(file);
        }
    }

    private void loadColorMap(File file)
    {
        try 
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));

            // Hajo: First line should be "256". Error check?
            String line = reader.readLine();

            int count = Integer.parseInt(line);
            Color [] colormap = new Color [count];

            for(int i=0; i<count; i++) 
            {
                line = reader.readLine();
                final String [] rgb = line.split(" ");

                int R = Integer.parseInt(rgb[0]);
                int G = Integer.parseInt(rgb[1]);
                int B = Integer.parseInt(rgb[2]);

                colormap[i] = new Color(R, G, B);
            }

            reader.close();
            colorPalette.setColors(colormap);
        } 
        catch(IOException ex) 
        {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void saveColorMap(File file)
    {
        try 
        {
            FileWriter writer = new FileWriter(file);

            writer.write("256\n");

            for(int i=0; i<256; i++) 
            {
                Color color = colorPalette.getColor(i);
                writer.write("" + 
                             color.getRed() + " " +
                             color.getGreen() + " " +
                             color.getBlue() + "\n");
            }

            writer.close();

        } 
        catch(IOException ex) 
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private Image selectImageFile()
    {
        try
        {
            final String filterList = ".gif|.png|.jpg|.jpeg";
            File result;
            result = fs.selectFile(tilesetSplitter, "Select an image to import:", filterList, "Image files",
                                   currentSingleImportFile, FileSelector.Mode.OPEN);

            if(result != null)
            {
                currentSingleImportFile = result;
                
                BufferedImage img = ImageIO.read(currentSingleImportFile);
                return img;
            }
        
        }  
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    
    /**
     * Imports a single image, such as a PNG or GIF file into the currently selected
     * tile. Image types are limited to those supported by Java.
     *
     * @author Hj. Malthaner
     */
    private void importSingleImage() 
    {
        Image img = selectImageFile();
        
        if(img != null) 
        {
            askImportImage(img);
        }
    }

    /**
     * Imports multiple, numbered image files, such as a PNG or GIF files. Import 
     * starts at the currently selected tile and continues from there.
     * Image types are limited to those supported by Java.
     *
     * @author Hj. Malthaner
     */
    private void importImageSequence() 
    {
        final String filterList = ".gif|.png|.jpg|.jpeg";
        File result;
        result = fs.selectFile(tilesetSplitter, "Select an image to import:", filterList, "Image files",
                               currentSingleImportFile, FileSelector.Mode.OPEN);

        if(result != null)
        {
            currentSingleImportFile = result;

            String name = result.getAbsolutePath();
            int pointI = name.lastIndexOf('.');
            
            // find numbered part of the file name
            int numberStart = pointI - 1;
            while(numberStart > 0 && Character.isDigit(name.charAt(numberStart)))
            {
                numberStart --;
            }
            numberStart ++;
            
            if(numberStart >= 0)
            {
                String nameBase = name.substring(0, numberStart);
                String nameSuffix = name.substring(pointI);
                
                String number = name.substring(numberStart, pointI);
                
                int numberWidth = number.length();
                int sequenceNumber = Integer.parseInt(number);
                
                int startTile = currentTile;
                
                while(true)
                {
                    number = makeSequenceNumber(numberWidth, sequenceNumber);
                    
                    File frame = new File(nameBase + number + nameSuffix);

                    if(frame.exists())
                    {
                        try 
                        {
                            BufferedImage img = ImageIO.read(frame);
                            askImportImage(img);
                            
                            currentTile ++;                           
                            sequenceNumber ++;
                        }
                        catch (IOException ex) 
                        {
                            Logger.getLogger(TilesetEditor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                    {
                        // end of frame sequence reached
                        break;
                    }
                }
                
                // currentTile = startTile;
                imageList.setSelectedIndex(currentTile);
                saveUndo();
                
            }
            else
            {
                requester.askChoice(this, "Can't find sequence number in filename.", "Bummer");
            }
            
        }
    }

    private void importTilesFromImage()
    {
        Image img = selectImageFile();

        if(img != null) 
        {
            importTilesFromImage(img);
        }
    }

    private void importTilesFromImage(Image img)
    {
        SimpleMessageBox box = new SimpleMessageBox(this,
                                                    "Tile Raster",
                                                    "Please enter the tile raster used in this image:",
                                                    "64x64");
        box.setVisible(true);

        String input = box.getInput();
        String [] parts = input.split("x");
        if(parts.length == 2) 
        {
            int rasterW = Integer.parseInt(parts[0]);
            int rasterH = Integer.parseInt(parts[1]);

            int w = img.getWidth(null);
            int h = img.getHeight(null);

            System.err.println("Importing " + rasterW + "x" + rasterH +
                               " tiles from a " + w + "x" + h + " image");

            BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics bgr = bim.getGraphics();
            bgr.drawImage(img, 0, 0, null);

            for(int y=0; y<=h-rasterH; y+=rasterH) 
            {
                for(int x=0; x<=w-rasterW; x+=rasterW) 
                {
                    if(currentTile < tileSet.size()) 
                    {
                        importImage(bim.getSubimage(x, y, rasterW, rasterH));
                        currentTile ++;
                    }
                }
            }
            currentTile --;
            imageList.setSelectedIndex(currentTile);
        }
    }

    private void multiImport(Image img)
    {
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics bgr = bim.getGraphics();
        bgr.drawImage(img, 0, 0, null);

        int back = bim.getRGB(0, 0);
        
        int xstart, xend;
        
        currentTile --;
        
        int x = 0;
        while(x < w) {
            boolean columnClean = true;

            while(columnClean && x < w) {
                for(int y=0; y<h; y++) {
                    if(bim.getRGB(x,y) != back) {
                        columnClean = false;
                    }
                }
                x++;
            }
            xstart = x-1;

            while(!columnClean && x < w) {
                columnClean = true;
                for(int y=0; y<h; y++) {
                    if(bim.getRGB(x,y) != back) {
                        columnClean = false;
                    }
                }
                x++;
            }
            
            xend = x;
            
            currentTile ++;
            askImportImage(bim.getSubimage(xstart, 0, (xend-xstart), h));
        }
        
    }
    
    public void askImportImage(final Image img)
    {
        final int width = img.getWidth(this);
        final int height = img.getHeight(this);
        
        // Hajo: ask only if canvas is too small.
        if(canvas.getWidth() < width || canvas.getHeight() < height)
        {
            String choice = requester.askChoice(this, 
                    "<html>Set canvas size "
                    + "(currently " + canvas.getWidth() + "x" + canvas.getHeight() + ")<br>"
                    + "to image size "
                    + "(" + width + "x" + height + ")"
                    + "?</html>",
                    "(y)es|(n)o");

            if("n".equals(choice))
            {
                // Keep canvas size
            }
            else
            {
                setCanvasSize(width, height);
            }
        }
        
        importImage(img);
    }

    private void importImage(final Image img)
    {
        clearImage(canvas);
        
        Graphics gr = canvas.getGraphics();
        gr.drawImage(img, 0, 0, this);

        imageClicked(currentTile);
    }
    
    /**
     * Exports a single image into a PNG file.
     *
     * @author Hj. Malthaner
     */
    private void exportSingleImage(boolean argb) 
    {
        final String format = "PNG";
        
        try
        {
            File selectedFile;
            
            if(tileSet.get(currentTile).getString(0) != null &&
               tileSet.get(currentTile).getString(0).length() > 1)
            {
                String name = tileSet.get(currentTile).getString(0).toLowerCase();
                name = name.replaceAll(" ", "_");
                
                selectedFile = new File(name + ".png");
            }
            else
            {
                selectedFile = new File("img_" + currentTile + ".png");
            }

            File result;
            result = fs.selectFile(tilesetSplitter, "Export PNG image:", ".png", "PNG files",
                                   currentSingleImportFile, selectedFile, FileSelector.Mode.WRITE);

            if(result != null)
            {
                String imageFileName = result.getAbsolutePath();
                System.err.println("You chose to write this file: " + imageFileName);
                currentSingleImportFile = result;
                
                if(argb)
                {
                    System.err.println("Writing PNG with alpha channel.");
                    ImageIO.write(tileSet.get(currentTile).img, format, currentSingleImportFile);
                }
                else
                {
                    System.err.println("Writing PNG without alpha channel.");

                    TileDescriptor tld = tileSet.get(currentTile);

                    final BufferedImage img = new BufferedImage(tld.img.getWidth(),
                                                                tld.img.getHeight(),
                                                                BufferedImage.TYPE_INT_RGB);
                    final Graphics gr = img.getGraphics();
                    
                    gr.setColor(new Color(255, 0, 255));
                    gr.fillRect(0, 0, 
                                tld.img.getWidth(),
                                tld.img.getHeight());
                    gr.drawImage(tld.img, 0, 0, null);
                    ImageIO.write(img, format, currentSingleImportFile);
                }
            }
        } 
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }        
    }
    
    /**
     * Exports a single image into a 8 bit GIF file.
     *
     * @author Hj. Malthaner
     */
    private void exportSingleImageGif() 
    {
        final String format = "GIF";
        
        try
        {
            File selectedFile;
            
            if(tileSet.get(currentTile).getString(0) != null &&
               tileSet.get(currentTile).getString(0).length() > 1)
            {
                String name = tileSet.get(currentTile).getString(0).toLowerCase();
                name = name.replaceAll(" ", "_");
                
                selectedFile = new File(name + ".gif");
            }
            else
            {
                selectedFile = new File("img_" + currentTile + ".gif");
            }

            File result;
            result = fs.selectFile(tilesetSplitter, "Export GIF image:", ".gif", "GIF files",
                                   currentSingleImportFile, selectedFile, FileSelector.Mode.WRITE);

            if(result != null)
            {
                String imageFileName = result.getAbsolutePath();
                System.err.println("You chose to write this file: " + imageFileName);
                currentSingleImportFile = result;
                

                TileDescriptor tld = tileSet.get(currentTile);

                final BufferedImage img = new BufferedImage(tld.img.getWidth(),
                                                            tld.img.getHeight(),
                                                            BufferedImage.TYPE_INT_RGB);
                final Graphics gr = img.getGraphics();

                gr.setColor(new Color(255, 0, 255));
                gr.fillRect(0, 0, 
                            tld.img.getWidth(),
                            tld.img.getHeight());
                gr.drawImage(tld.img, 0, 0, null);
                ImageIO.write(img, format, currentSingleImportFile);
            }
        } 
        catch (IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }        
    }
    
    /**
     * Called if an image is selected in the tileSetelector
     * 
     * @param tileNo The index number of the selected image.
     */
    public void imageClicked(final int tileNo)
    {
        if(paintingTool != null && tileNo != currentTile)
        {
            paintingTool.onTileWillChange(tileNo);
        }
        
        // Hajo: convert formerly edited image
        updateTileData(currentTile);
        clearImage(canvas);

        if(tileNo >= 0) 
        {
            // System.err.println("Display order=" + imageNumber + " ID=" + tileSet.get(imageNumber).tileId);

            final TileDescriptor tld = tileSet.get(tileNo);
            idField.setText("" + tld.tileId);

            updateCanvas(tld.img);
            showTileAttributes(tileNo);
            updateUnderlayOffset();
            footField.setText("" + tld.footX + ", " + tld.footY);

            importMenuItem.setEnabled(true);  // Hajo: selection there -> now we can read tileSet
        } 
        else 
        {
            idField.setText("0");
        }

        if(tileNo != currentTile)
        {
            saveUndo();
            currentTile = tileNo;
            
            if(backgroundTileSet != null)
            {
                backgroundTile = backgroundTileSet.get(currentTile);
                imageView.setUnderlay(backgroundTile.img);
                updateUnderlayOffset();
            }
        }
        
        imageView.repaint(50);
    }

    private void updateCanvas(BufferedImage image)
    {
        if(image.getHeight() > canvas.getHeight() || image.getWidth() > canvas.getWidth())
        {
            // Hajo: this tile needs a bigger canvas
            setCanvasSize(image.getWidth(), image.getHeight());
            clearImage(canvas);
        }
        
        for(int j=0; j<image.getHeight(); j++) {
            for(int i=0; i<image.getWidth(); i++) {
                final int argb = image.getRGB(i, j);

                // Better to preserve alpha?
                /*
                if((argb & 0xFF000000) != 0) {
                    canvas.setRGB(i, j, argb | 0xFF000000);
                }
                */
                if((argb & 0xFF000000) != 0) {
                    canvas.setRGB(i, j, argb);
                }
            }
        }
    }

    private void showTileAttributes(final int tileNo)
    {
        final TileDescriptor tld = tileSet.get(tileNo);

        for(int i=0; i<stringPanel.fields.length; i++) {
            stringPanel.fields[i].setText(tld.getString(i));
        }
        for(int i=0; i<intPanel.fields.length; i++) {
            intPanel.fields[i].setText("" + tld.getInt(i));
        }
    }

    /**
     * This is called once the user clicked a color. The index of the 
     * selected color is passed as parameter.
     *
     * @param colorIndex The index of the selected color
     */
    @Override
    public void onColorSelected(int colorIndex)
    {
        paintColor = colorPalette.getColor(colorIndex);
        paintingTool.onColorSelected(colorIndex);
        
        // System.err.println("colorIndex = " + colorIndex);
    }


    /**
     * Update data of currently selected tile.
     */
    public void updateTileData()
    {
        updateTileData(currentTile);
    }

    private void updateTileData(int tileNo)
    {
        if(tileNo >= 0) {
            ItemConfiguration config = tileSet.getTileConfiguration();

            final String [] strings = stringPanel.getStringValues();
            final int [] ints = intPanel.getIntValues();

            TileDescriptor tld = new TileDescriptor(config, strings, ints);
            TileDescriptor old = tileSet.get(tileNo);
            if(old != null) 
            {
                tld.offX = old.offX;
                tld.offY = old.offY;
                tld.footX = old.footX;
                tld.footY = old.footY;
                tld.tileId = old.tileId;
            }
            tileSet.set(tileNo, tld);
            updateImageData(tileNo);
        }
    }


    /**
     * This method takes the image from the canvas and updates
     * the tile descriptor image from its data
     */
    private void updateImageData(int tileNo)
    {
        if(tileNo >= 0) {
            int maxX = 0;
            int maxY = 0;

            int colorZero = colorPalette.getColor(0).getRGB();

            // Hajo: Scan for bounds
            for(int y=0; y<canvas.getHeight(); y++) {
                for(int x=0; x<canvas.getWidth(); x++) {
                    final int argb = canvas.getRGB(x, y);
                    final int a = (argb >>> 24) & 255;

                    if(a > 254 && argb != colorZero) 
                    {
                        if(x > maxX) 
                        {
                            maxX = x;
                        }
                        if(y > maxY) 
                        {
                            maxY = y;
                        }
                    }
                }
            }

            // System.err.println("Image dimensions are: " + maxX + "x" + maxY);
            final int width = maxX + 1;
            final int height = maxY + 1;

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            
            for(int y=0; y<height; y++) {
                for(int x=0; x<width; x++) {
                    final int argb = canvas.getRGB(x, y);
                    final int a = (argb >>> 24) & 255;

                    if(a > 254 && argb != colorZero) {
                        img.setRGB(x, y, argb);
                    } else {
                        img.setRGB(x, y, 0);
                    }
                }
            }
            
            tileSet.get(tileNo).img = img;

            refreshListEntry(tileNo);

            previewFrame.setImage(img);
        }
    }

    private void refreshListEntry(final int tileNo)
    {
        DefaultListModel model = (DefaultListModel)imageList.getModel();
        if(tileNo < model.getSize()) {
            model.set(tileNo, tileSet.get(tileNo));
        }
    }

    private void swapTiles(int index1, int index2)
    {
        if(index1 >= 0 && index1 < tileSet.size() &&
           index2 >= 0 && index2 < tileSet.size())
        {
            TileDescriptor one = tileSet.get(index1);
            TileDescriptor two = tileSet.get(index2);

            tileSet.set(index1, two);
            tileSet.set(index2, one);
            
            // refreshListEntry(index1);
            // refreshListEntry(index2);

            updateForNewImageList();
            imageList.setSelectedIndex(index2);
        }
    }

    /**
     * Clear the image by overwriting the rgb
     * data with transparent color.
     */
    private void clearImage(final BufferedImage img)
    {
        final int width = img.getWidth();
        final int height = img.getHeight();
        
        if(clearPixels.length != width * height)
        {
            clearPixels = new int[width*height];    
        }
        
        img.setRGB(0, 0, width, height, clearPixels, 0, width);
    }

    private void setCanvasSize(final int width, final int height)
    {
        canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        clearImage(canvas);
        canvas.getGraphics().drawImage(undoCanvas, 0, 0, null);

        undoCanvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        imageView.setImage(canvas);
    }

    private boolean expandCanvasIfNeeded(int width, int height)
    {
        boolean needNewSize = false;

        if(width > canvas.getWidth())
        {
            needNewSize = true;
        }
        else
        {
            width = canvas.getWidth();
        }

        if(height > canvas.getHeight())
        {
            needNewSize = true;
        }
        else
        {
            height = canvas.getHeight();
        }

        if(needNewSize)
        {
            setCanvasSize(width, height);
        }

        return needNewSize;
    }

    private void showIsoPreview()
    {
        updateTileSetRaster();

        previewFrame.setIsometric(true);
        previewFrame.setRaster(tileSet.rasterX*2, tileSet.rasterY);

        showTilingPreview();
    }

    private void showTiledPreview()
    {
        updateTileSetRaster();

        previewFrame.setIsometric(false);
        previewFrame.setRaster(tileSet.rasterX, tileSet.rasterY);

        showTilingPreview();
    }

    private void showTilingPreview()
    {
        if(tileSet.rasterX == 0 || tileSet.rasterY == 0) 
        {
            SimpleMessageBox box = new SimpleMessageBox(this,
                                                        "Tile Raster Problem",
                                                        "Can't show preview for a tile raster width or height of zero!",
                                                        null);
            box.setOneButtonOnly(true);
            box.setVisible(true);
            box.dispose();

        }
        else 
        {
            previewFrame.setImage(tileSet.get(currentTile).img);
            previewFrame.setBackground(imageView.getBackground());

            previewFrame.setVisible(true);
        }
    }

    /**
     * Save one step of undo data
     */
    private void saveUndo() 
    {
        // System.err.println("Saving undo data");
        
        clearImage(undoCanvas);
        final Graphics ugr = undoCanvas.getGraphics();
        ugr.drawImage(canvas, 0, 0, null);
    }

    public void undo()
    {
        clearImage(canvas);
        final Graphics gr = canvas.getGraphics();
        gr.drawImage(undoCanvas, 0, 0, null);
    }

    public void setUndo(BufferedImage img)
    {
        clearImage(undoCanvas);
        final Graphics ugr = undoCanvas.getGraphics();
        ugr.drawImage(img, 0, 0, null);
    }

    private void restoreWindowState()
    {
        try 
        {
            final String prefsPath = prefsPath();

            File file = new File(prefsPath + ".tilemaster" + File.separator + "tilemaster.properties");

            if(file.exists()) 
            {
                System.err.println("Restoring from: " + file.getAbsolutePath());

                FileReader reader = new FileReader(file);

                Properties props = new Properties();
                props.load(reader);
                reader.close();

                int x = Integer.parseInt(props.getProperty("window.location.x", "0"));
                int y = Integer.parseInt(props.getProperty("window.location.y", "0"));
                int w = Integer.parseInt(props.getProperty("window.width", "784"));
                int h = Integer.parseInt(props.getProperty("window.height", "568"));
                int d = Integer.parseInt(props.getProperty("divider.location", "375"));

                setLocation(x, y);
                setSize(w, h);

                tilesetSplitter.setDividerLocation(d);
                
                // restore old fs path selection
                
                DefaultListModel paths = fs.getPathModel();
                paths.clear();
                
                int i=0;
                String path;
                
                while((path = props.getProperty("path." + i)) != null)
                {
                    if(path.startsWith("One "))
                    {
                        // special case "one directory up"
                        paths.addElement(path);
                    }
                    else
                    {
                        File fspath = new File(path);
                        
                        // Only add valid paths, prune stale paths.
                        if(fspath.exists())
                        {
                            paths.addElement(new FileWrapper(fspath));
                        }
                    }
                    i++;
                }
            }

            File colormapFile = new File(prefsPath + ".tilemaster" + File.separator + "colormap.pal");
            if(colormapFile.exists())
            {
                loadColorMap(colormapFile);
            }

        }
        catch(IOException ex) 
        {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private void saveWindowState()
    {
        try 
        {
            
            Properties props = new Properties();

            Point pos = getLocation();

            props.put("window.width", "" + getWidth());
            props.put("window.height", "" + getHeight());
            props.put("window.location.x", "" + pos.x);
            props.put("window.location.y", "" + pos.y);
            props.put("divider.location", "" + tilesetSplitter.getDividerLocation());

            // props.put("canvas.width", "" + canvas.getWidth());
            // props.put("canvas.height", "" + canvas.getHeight());

            DefaultListModel paths = fs.getPathModel();
            
            for(int i=0; i<paths.size(); i++)
            {
                Object o = paths.get(i);
                
                if(o instanceof FileWrapper)
                {
                    o = ((FileWrapper)o).file;
                }
                String path = "" + o;
                props.put("path." + i, path);
            }
                        
            final String prefsPath = prefsPath();

            // System.err.println("prefsPath " + prefsPath);

            File file = new File(prefsPath + ".tilemaster");
            file.mkdirs();
            file = new File(file.getAbsolutePath() + File.separator + "tilemaster.properties");

            // System.err.println("Saving to: " + file.getAbsolutePath());

            FileWriter writer = new FileWriter(file);
            props.store(writer, "Tilemaster Settings");
            writer.close();

            File colormapFile = new File(prefsPath + ".tilemaster" + File.separator + "colormap.pal");
            saveColorMap(colormapFile);

        }
        catch(IOException ex) 
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    private String prefsPath()
    {

        String portable = System.getProperty("tmPortable");
        final String prefsPath;
        
        if(portable != null && "true".equalsIgnoreCase(portable))
        {
            prefsPath = "." + File.separator;
        }
        else
        {
           prefsPath = System.getProperty("user.home") + File.separator;
        }
        
        return prefsPath;
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        toolButtonGroup = new javax.swing.ButtonGroup();
        tilesetSplitter = new javax.swing.JSplitPane();
        upperPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        stringsContainer = new javax.swing.JPanel();
        stringScroller = new javax.swing.JScrollPane();
        intsContainer = new javax.swing.JPanel();
        intScroller = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rasterField = new javax.swing.JTextField();
        colorsPanelContainer = new javax.swing.JPanel();
        toolPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        coordLabel = new javax.swing.JLabel();
        coordField = new javax.swing.JLabel();
        offsetLabel = new javax.swing.JLabel();
        offsetField = new javax.swing.JLabel();
        idLabel = new javax.swing.JLabel();
        idField = new javax.swing.JLabel();
        footLabel = new javax.swing.JLabel();
        footField = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        paintToolsPanel = new javax.swing.JPanel();
        btPoint = new javax.swing.JToggleButton();
        btLine = new javax.swing.JToggleButton();
        btRect = new javax.swing.JToggleButton();
        btOval = new javax.swing.JToggleButton();
        btRoundRect = new javax.swing.JToggleButton();
        btAntiAlias = new javax.swing.JToggleButton();
        btSpray = new javax.swing.JToggleButton();
        btSpraySetup = new javax.swing.JToggleButton();
        cbPaintFilled = new javax.swing.JCheckBox();
        colorToolsPanel = new javax.swing.JPanel();
        btFill = new javax.swing.JToggleButton();
        btRecolor = new javax.swing.JToggleButton();
        btUnrange = new javax.swing.JButton();
        btFillOutline = new javax.swing.JToggleButton();
        btEditor = new javax.swing.JButton();
        btFillGradient = new javax.swing.JToggleButton();
        btFillContour = new javax.swing.JToggleButton();
        toleranceSpinner = new javax.swing.JSpinner();
        toleranceLabel = new javax.swing.JLabel();
        btShadingFill = new javax.swing.JToggleButton();
        btShadeBorder = new javax.swing.JToggleButton();
        selectionToolsPanel = new javax.swing.JPanel();
        btSelection = new javax.swing.JToggleButton();
        btSelectRectangle = new javax.swing.JToggleButton();
        btSelectAsMask = new javax.swing.JToggleButton();
        btSelectClear = new javax.swing.JToggleButton();
        btPolyExtract = new javax.swing.JToggleButton();
        btPolyCopy = new javax.swing.JToggleButton();
        pluginToolsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        toolPluginList = new javax.swing.JList();
        paddingPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        imagePanelContainer = new javax.swing.JPanel();
        imageListContainer = new javax.swing.JPanel();
        imageListScroller = new javax.swing.JScrollPane();
        imageList = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        moveRightButton = new javax.swing.JButton();
        moveLeftButton = new javax.swing.JButton();
        theMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        editMenu = new javax.swing.JMenu();
        transformsMenu = new javax.swing.JMenu();
        brushMenu = new javax.swing.JMenu();
        brushFromDot = new javax.swing.JMenuItem();
        brushFromSelection = new javax.swing.JMenuItem();
        previewMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        tilesetSplitter.setDividerLocation(500);
        tilesetSplitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        tilesetSplitter.setResizeWeight(0.7);
        tilesetSplitter.setOneTouchExpandable(true);

        upperPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setMinimumSize(new java.awt.Dimension(240, 200));
        leftPanel.setLayout(new java.awt.GridBagLayout());

        stringsContainer.setBorder(javax.swing.BorderFactory.createTitledBorder("Tile String Attributes"));
        stringsContainer.setMinimumSize(new java.awt.Dimension(64, 160));
        stringsContainer.setLayout(new java.awt.BorderLayout());

        stringScroller.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        stringScroller.setPreferredSize(new java.awt.Dimension(252, 260));
        stringsContainer.add(stringScroller, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(16, 0, 0, 0);
        leftPanel.add(stringsContainer, gridBagConstraints);

        intsContainer.setBorder(javax.swing.BorderFactory.createTitledBorder("Tile Integer Attributes"));
        intsContainer.setMinimumSize(new java.awt.Dimension(64, 160));
        intsContainer.setLayout(new java.awt.BorderLayout());

        intScroller.setPreferredSize(new java.awt.Dimension(252, 240));
        intsContainer.add(intScroller, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        leftPanel.add(intsContainer, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Tileset Parameters"));

        jLabel1.setText("Raster:");
        jPanel3.add(jLabel1);

        rasterField.setColumns(8);
        rasterField.setText("16x16");
        rasterField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rasterFieldActionPerformed(evt);
            }
        });
        jPanel3.add(rasterField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        leftPanel.add(jPanel3, gridBagConstraints);

        upperPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        colorsPanelContainer.setPreferredSize(new java.awt.Dimension(224, 256));
        colorsPanelContainer.setLayout(new java.awt.BorderLayout());

        toolPanel.setLayout(new java.awt.GridBagLayout());

        jPanel4.setMinimumSize(new java.awt.Dimension(64, 50));
        jPanel4.setPreferredSize(new java.awt.Dimension(64, 50));
        jPanel4.setLayout(null);

        coordLabel.setText("Mouse:");
        jPanel4.add(coordLabel);
        coordLabel.setBounds(0, 0, 50, 15);

        coordField.setText("0, 0");
        coordField.setToolTipText("Mouse pointer position");
        coordField.setMinimumSize(new java.awt.Dimension(50, 14));
        jPanel4.add(coordField);
        coordField.setBounds(50, 0, 60, 15);

        offsetLabel.setText("Offset:");
        jPanel4.add(offsetLabel);
        offsetLabel.setBounds(110, 0, 50, 15);

        offsetField.setText("0, 0");
        offsetField.setToolTipText("Tile offset");
        offsetField.setMinimumSize(new java.awt.Dimension(50, 14));
        jPanel4.add(offsetField);
        offsetField.setBounds(150, 0, 60, 15);

        idLabel.setText("ID:");
        jPanel4.add(idLabel);
        idLabel.setBounds(110, 20, 30, 15);

        idField.setText("0");
        idField.setToolTipText("Tile ID, unique within a set.");
        jPanel4.add(idField);
        idField.setBounds(150, 20, 60, 15);

        footLabel.setText("Foot:");
        jPanel4.add(footLabel);
        footLabel.setBounds(0, 20, 50, 15);

        footField.setText("0, 0");
        jPanel4.add(footField);
        footField.setBounds(50, 20, 60, 15);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 16, 8, 0);
        toolPanel.add(jPanel4, gridBagConstraints);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        paintToolsPanel.setLayout(new java.awt.GridBagLayout());

        toolButtonGroup.add(btPoint);
        btPoint.setText("Draw");
        btPoint.setToolTipText("Draw freehand");
        btPoint.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btPoint.setPreferredSize(new java.awt.Dimension(98, 26));
        btPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPointActionPerformed(evt);
            }
        });
        paintToolsPanel.add(btPoint, new java.awt.GridBagConstraints());

        toolButtonGroup.add(btLine);
        btLine.setText("Line");
        btLine.setToolTipText("Draw lines");
        btLine.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btLine.setPreferredSize(new java.awt.Dimension(98, 26));
        btLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btLineActionPerformed(evt);
            }
        });
        paintToolsPanel.add(btLine, new java.awt.GridBagConstraints());

        toolButtonGroup.add(btRect);
        btRect.setText("Rectangle");
        btRect.setToolTipText("Paint rectangles. Press ALT to paint filled rectangles");
        btRect.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btRect.setPreferredSize(new java.awt.Dimension(98, 26));
        btRect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        paintToolsPanel.add(btRect, gridBagConstraints);

        toolButtonGroup.add(btOval);
        btOval.setText("Oval");
        btOval.setToolTipText("Paint ovals. Press ALT to paint filled ovals");
        btOval.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btOval.setPreferredSize(new java.awt.Dimension(98, 26));
        btOval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btOvalActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        paintToolsPanel.add(btOval, gridBagConstraints);

        toolButtonGroup.add(btRoundRect);
        btRoundRect.setText("Rounded");
        btRoundRect.setToolTipText("Draw rounded rectangles");
        btRoundRect.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btRoundRect.setPreferredSize(new java.awt.Dimension(98, 26));
        btRoundRect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRoundRectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        paintToolsPanel.add(btRoundRect, gridBagConstraints);

        toolButtonGroup.add(btAntiAlias);
        btAntiAlias.setText("Anti Alias");
        btAntiAlias.setToolTipText("Anti alias a pixel - interpolate surrounding colors");
        btAntiAlias.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btAntiAlias.setPreferredSize(new java.awt.Dimension(98, 26));
        btAntiAlias.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAntiAliasActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        paintToolsPanel.add(btAntiAlias, gridBagConstraints);

        toolButtonGroup.add(btSpray);
        btSpray.setText("Spraycan");
        btSpray.setPreferredSize(new java.awt.Dimension(98, 26));
        btSpray.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSprayActionPerformed(evt);
            }
        });
        paintToolsPanel.add(btSpray, new java.awt.GridBagConstraints());

        toolButtonGroup.add(btSpraySetup);
        btSpraySetup.setText("Spray Setup");
        btSpraySetup.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btSpraySetup.setPreferredSize(new java.awt.Dimension(98, 26));
        btSpraySetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSpraySetupActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        paintToolsPanel.add(btSpraySetup, gridBagConstraints);

        cbPaintFilled.setText("Fill Shapes");
        cbPaintFilled.setToolTipText("Select to change to filled drawing.");
        cbPaintFilled.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 16, 0);
        paintToolsPanel.add(cbPaintFilled, gridBagConstraints);

        jTabbedPane1.addTab("Paint", paintToolsPanel);

        colorToolsPanel.setLayout(new java.awt.GridBagLayout());

        toolButtonGroup.add(btFill);
        btFill.setText("Flood Fill");
        btFill.setToolTipText("Flood fill area with selected color");
        btFill.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btFill.setPreferredSize(new java.awt.Dimension(98, 26));
        btFill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFillActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        colorToolsPanel.add(btFill, gridBagConstraints);

        toolButtonGroup.add(btRecolor);
        btRecolor.setText("Recolor");
        btRecolor.setToolTipText("Replace a color range with another");
        btRecolor.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btRecolor.setPreferredSize(new java.awt.Dimension(98, 26));
        btRecolor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRecolorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        colorToolsPanel.add(btRecolor, gridBagConstraints);

        btUnrange.setText("Extract");
        btUnrange.setToolTipText("Extract a color range");
        btUnrange.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btUnrange.setPreferredSize(new java.awt.Dimension(98, 26));
        btUnrange.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btUnrangeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        colorToolsPanel.add(btUnrange, gridBagConstraints);

        toolButtonGroup.add(btFillOutline);
        btFillOutline.setText("Outline");
        btFillOutline.setToolTipText("Outline area with selected color");
        btFillOutline.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btFillOutline.setPreferredSize(new java.awt.Dimension(98, 26));
        btFillOutline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFillOutlineActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        colorToolsPanel.add(btFillOutline, gridBagConstraints);

        btEditor.setText("Edit Colors");
        btEditor.setToolTipText("Open the color palette editor.");
        btEditor.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btEditor.setPreferredSize(new java.awt.Dimension(98, 26));
        btEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btEditorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 4;
        colorToolsPanel.add(btEditor, gridBagConstraints);

        toolButtonGroup.add(btFillGradient);
        btFillGradient.setText("Gradients");
        btFillGradient.setToolTipText("Gradient fill tools.");
        btFillGradient.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btFillGradient.setPreferredSize(new java.awt.Dimension(98, 26));
        btFillGradient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFillGradientActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        colorToolsPanel.add(btFillGradient, gridBagConstraints);

        toolButtonGroup.add(btFillContour);
        btFillContour.setText("Contour Fill");
        btFillContour.setToolTipText("Special contour fill tools.");
        btFillContour.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btFillContour.setPreferredSize(new java.awt.Dimension(98, 26));
        btFillContour.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btFillContourActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        colorToolsPanel.add(btFillContour, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        colorToolsPanel.add(toleranceSpinner, gridBagConstraints);

        toleranceLabel.setText("Tolerance:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        colorToolsPanel.add(toleranceLabel, gridBagConstraints);

        toolButtonGroup.add(btShadingFill);
        btShadingFill.setText("Shading Fill");
        btShadingFill.setToolTipText("Gradient shading tools.");
        btShadingFill.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btShadingFill.setPreferredSize(new java.awt.Dimension(98, 26));
        btShadingFill.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btShadingFillActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        colorToolsPanel.add(btShadingFill, gridBagConstraints);

        toolButtonGroup.add(btShadeBorder);
        btShadeBorder.setText("Borderblend");
        btShadeBorder.setToolTipText("");
        btShadeBorder.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btShadeBorder.setPreferredSize(new java.awt.Dimension(98, 26));
        btShadeBorder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btShadeBorderActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        colorToolsPanel.add(btShadeBorder, gridBagConstraints);

        jTabbedPane1.addTab("Color", colorToolsPanel);

        selectionToolsPanel.setLayout(new java.awt.GridBagLayout());

        toolButtonGroup.add(btSelection);
        btSelection.setText("Rubberband");
        btSelection.setToolTipText("Select, copy and paste image areas");
        btSelection.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btSelection.setPreferredSize(new java.awt.Dimension(98, 26));
        btSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSelectionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        selectionToolsPanel.add(btSelection, gridBagConstraints);

        toolButtonGroup.add(btSelectRectangle);
        btSelectRectangle.setText("Rectangle");
        btSelectRectangle.setToolTipText("Select a fixed size rectangular area");
        btSelectRectangle.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btSelectRectangle.setPreferredSize(new java.awt.Dimension(98, 26));
        btSelectRectangle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSelectRectangleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        selectionToolsPanel.add(btSelectRectangle, gridBagConstraints);

        toolButtonGroup.add(btSelectAsMask);
        btSelectAsMask.setText("As Mask");
        btSelectAsMask.setToolTipText("Use selection as mask");
        btSelectAsMask.setPreferredSize(new java.awt.Dimension(98, 26));
        btSelectAsMask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSelectAsMaskActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        selectionToolsPanel.add(btSelectAsMask, gridBagConstraints);

        toolButtonGroup.add(btSelectClear);
        btSelectClear.setText("Clear");
        btSelectClear.setToolTipText("Clear selection");
        btSelectClear.setMargin(new java.awt.Insets(2, 0, 2, 0));
        btSelectClear.setPreferredSize(new java.awt.Dimension(98, 26));
        btSelectClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSelectClearActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        selectionToolsPanel.add(btSelectClear, gridBagConstraints);

        toolButtonGroup.add(btPolyExtract);
        btPolyExtract.setText("Extract Poly");
        btPolyExtract.setToolTipText("Extract a shape by drawing a polygon around it");
        btPolyExtract.setPreferredSize(new java.awt.Dimension(98, 26));
        btPolyExtract.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPolyExtractActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        selectionToolsPanel.add(btPolyExtract, gridBagConstraints);

        toolButtonGroup.add(btPolyCopy);
        btPolyCopy.setText("Copy Poly");
        btPolyCopy.setToolTipText("Extract a shape by drawing a polygon around it");
        btPolyCopy.setPreferredSize(new java.awt.Dimension(98, 26));
        btPolyCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btPolyCopyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        selectionToolsPanel.add(btPolyCopy, gridBagConstraints);

        jTabbedPane1.addTab("Select", selectionToolsPanel);

        pluginToolsPanel.setToolTipText("Plugins");
        pluginToolsPanel.setLayout(new java.awt.BorderLayout());

        toolPluginList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        toolPluginList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                toolPluginListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(toolPluginList);

        pluginToolsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("", new javax.swing.ImageIcon(getClass().getResource("/tilemaster/editor/mastertile.png")), pluginToolsPanel); // NOI18N

        jPanel2.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 8);
        toolPanel.add(jPanel2, gridBagConstraints);

        paddingPanel.setPreferredSize(new java.awt.Dimension(10, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.weighty = 0.7;
        toolPanel.add(paddingPanel, gridBagConstraints);

        colorsPanelContainer.add(toolPanel, java.awt.BorderLayout.SOUTH);

        upperPanel.add(colorsPanelContainer, java.awt.BorderLayout.EAST);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        imagePanelContainer.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        imagePanelContainer.setDoubleBuffered(false);
        imagePanelContainer.setMinimumSize(new java.awt.Dimension(256, 256));
        imagePanelContainer.setOpaque(false);
        imagePanelContainer.setPreferredSize(new java.awt.Dimension(258, 258));
        imagePanelContainer.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 2, 0);
        jPanel1.add(imagePanelContainer, gridBagConstraints);

        upperPanel.add(jPanel1, java.awt.BorderLayout.CENTER);

        tilesetSplitter.setLeftComponent(upperPanel);

        imageListContainer.setBackground(java.awt.Color.gray);
        imageListContainer.setMinimumSize(new java.awt.Dimension(128, 115));
        imageListContainer.setPreferredSize(new java.awt.Dimension(10, 128));
        imageListContainer.setLayout(new java.awt.BorderLayout());

        imageList.setBackground(java.awt.Color.gray);
        imageList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        imageList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        imageList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        imageList.setVisibleRowCount(-1);
        imageList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                imageListValueChanged(evt);
            }
        });
        imageList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                imageListKeyPressed(evt);
            }
        });
        imageListScroller.setViewportView(imageList);

        imageListContainer.add(imageListScroller, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.GridLayout(2, 1));

        moveRightButton.setText(">>");
        moveRightButton.setToolTipText("Move current tile up in the tile set.");
        moveRightButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        moveRightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveRightButtonActionPerformed(evt);
            }
        });
        jPanel5.add(moveRightButton);

        moveLeftButton.setText("<<");
        moveLeftButton.setToolTipText("Move current tile down in the tile set.");
        moveLeftButton.setMargin(new java.awt.Insets(2, 0, 2, 0));
        moveLeftButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveLeftButtonActionPerformed(evt);
            }
        });
        jPanel5.add(moveLeftButton);

        imageListContainer.add(jPanel5, java.awt.BorderLayout.WEST);

        tilesetSplitter.setRightComponent(imageListContainer);

        getContentPane().add(tilesetSplitter, java.awt.BorderLayout.CENTER);

        fileMenu.setText("File");
        theMenuBar.add(fileMenu);

        editMenu.setText("Edit");
        theMenuBar.add(editMenu);

        transformsMenu.setText("Transforms");
        theMenuBar.add(transformsMenu);

        brushMenu.setText("Brush");

        brushFromDot.setText("Standard Dot");
        brushFromDot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brushFromDotActionPerformed(evt);
            }
        });
        brushMenu.add(brushFromDot);

        brushFromSelection.setText("From Selection");
        brushFromSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brushFromSelectionActionPerformed(evt);
            }
        });
        brushMenu.add(brushFromSelection);

        theMenuBar.add(brushMenu);

        previewMenu.setText("Preview");
        theMenuBar.add(previewMenu);

        helpMenu.setText("Help");
        theMenuBar.add(helpMenu);

        setJMenuBar(theMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btLineActionPerformed
        setPaintingTool(new LineTool());
    }//GEN-LAST:event_btLineActionPerformed

    private void btUnrangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btUnrangeActionPerformed
        btPoint.setSelected(false);
        btFill.setSelected(false);
        btRect.setSelected(false);
        setPaintingTool(new RecolorTool(this, canvas, colorPalette, true));
    }//GEN-LAST:event_btUnrangeActionPerformed

    private void btSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSelectionActionPerformed
        selectRubberbandSelection();
    }//GEN-LAST:event_btSelectionActionPerformed

    private void btRecolorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRecolorActionPerformed
        setPaintingTool(new RecolorTool(this, canvas, colorPalette, false));
    }//GEN-LAST:event_btRecolorActionPerformed

    private void btFillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFillActionPerformed
        startFillmachine(new ColorFiller());
    }//GEN-LAST:event_btFillActionPerformed

    private void btPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btPointActionPerformed
        setPaintingTool(new PlotTool());
    }//GEN-LAST:event_btPointActionPerformed

    private void btRectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRectActionPerformed
        setPaintingTool(new FillRectTool());
    }//GEN-LAST:event_btRectActionPerformed

    private void imageListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_imageListValueChanged
        imageClicked(imageList.getSelectedIndex());
    }//GEN-LAST:event_imageListValueChanged

    private void btOvalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btOvalActionPerformed
        setPaintingTool(new OvalTool());
    }//GEN-LAST:event_btOvalActionPerformed

    private void btFillOutlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFillOutlineActionPerformed
        startFillmachine(new OutlineFiller());
    }//GEN-LAST:event_btFillOutlineActionPerformed

    private void btEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditorActionPerformed
        final PaletteEditorFrame pef = new PaletteEditorFrame();
        pef.setColors(colorPalette.getColors());

        pef.addColorListener(new ColorPaletteInterface() 
        {
            @Override
            public void onColorSelected(int colorIndex) 
            {
                colorPalette.setColors(pef.getColors());
            }
        });

        pef.setVisible(true);
    }//GEN-LAST:event_btEditorActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        previewFrame.setVisible(false);
        previewFrame.dispose();
        saveWindowState();
    }//GEN-LAST:event_formWindowClosed

    private void btAntiAliasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAntiAliasActionPerformed
        setPaintingTool(new InterpolateTool(canvas));
    }//GEN-LAST:event_btAntiAliasActionPerformed

    private void rasterFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rasterFieldActionPerformed
        updateTileSetRaster();
    }//GEN-LAST:event_rasterFieldActionPerformed

    private void moveRightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveRightButtonActionPerformed
        final int n = imageList.getSelectedIndex();
        swapTiles(n, n+1);
    }//GEN-LAST:event_moveRightButtonActionPerformed

    private void moveLeftButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveLeftButtonActionPerformed
        final int n = imageList.getSelectedIndex();
        swapTiles(n, n-1);
    }//GEN-LAST:event_moveLeftButtonActionPerformed

    private void toolPluginListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_toolPluginListValueChanged
        final int toolIndex = toolPluginList.getSelectedIndex();

        if(toolIndex > -1)
        {
            btHiddenExtra.setSelected(true);
            Object pluginSelection = toolPluginList.getSelectedValue();

            if(pluginSelection instanceof PluginListWrapper)
            {
                PluginListWrapper wrap = (PluginListWrapper)pluginSelection;
                setPaintingTool(wrap.tool);
            }
            else
            {
                // Testing mode
            }
        } 
        else
        {
            btPoint.setSelected(true);
            btPointActionPerformed(null);
        }
    }//GEN-LAST:event_toolPluginListValueChanged

    private void btFillGradientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFillGradientActionPerformed

        final String userSelection =
            requester.askChoice(this,
                    "Please select the gradient direction:",
                    "(H)orizontal|(V)ertical|Diagonaly (D)own)|Diagonaly (U)p");
        
        Filler filler = null;
        
        switch(userSelection.charAt(0))
        {
            case 'h':
                filler = new ContourFiller(this, colorPalette, 0, Math.PI);
                break;
            case 'v':
                filler = new ContourFiller(this, colorPalette, Math.PI/2.0, Math.PI);
                break;
            case 'u':
                filler = new ContourFiller(this, colorPalette, Math.PI/4.0, Math.PI);
                break;
            case 'd':
                filler = new ContourFiller(this, colorPalette, Math.PI*3/4.0, Math.PI);
                break;
        }
        
        /*
        switch(selection.charAt(0))
        {
            case 'h':
                filler = new ContourBrightnessFiller(this, colorPalette, 0, Math.PI);
                break;
            case 'v':
                filler = new ContourBrightnessFiller(this, colorPalette, Math.PI/2.0, Math.PI);
                break;
            case 'u':
                filler = new ContourBrightnessFiller(this, colorPalette, Math.PI/4.0, Math.PI);
                break;
            case 'd':
                filler = new ContourBrightnessFiller(this, colorPalette, Math.PI*3/4.0, Math.PI);
                break;
        }
        */

        startFillmachine(filler);
    }//GEN-LAST:event_btFillGradientActionPerformed

    private void btRoundRectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRoundRectActionPerformed
        setPaintingTool(new RoundRectTool());
    }//GEN-LAST:event_btRoundRectActionPerformed

    private void btFillContourActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btFillContourActionPerformed
        startFillmachine(new ContourFiller(this, colorPalette, Math.PI/48.0, Math.PI/24.0));        
    }//GEN-LAST:event_btFillContourActionPerformed

    private void btSelectRectangleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSelectRectangleActionPerformed
        setPaintingTool(new RectangleSelection(this));
    }//GEN-LAST:event_btSelectRectangleActionPerformed

    private void btSelectClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSelectClearActionPerformed
        selection.clear();
        imageView.setOverlay(null);
        imageView.repaint();
    }//GEN-LAST:event_btSelectClearActionPerformed

    private void btShadingFillActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btShadingFillActionPerformed
        startFillmachine(new ContourBrightnessFiller(this, colorPalette, Math.PI/48.0, Math.PI/24.0));
    }//GEN-LAST:event_btShadingFillActionPerformed

    private void btShadeBorderActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btShadeBorderActionPerformed
    {//GEN-HEADEREND:event_btShadeBorderActionPerformed
        blendAmount = requester.askNumber(this, "Blending power (0-100):", blendAmount);
        startFillmachine(new OutlineBlendFiller(blendAmount*256/100));
    }//GEN-LAST:event_btShadeBorderActionPerformed

    private void btSelectAsMaskActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btSelectAsMaskActionPerformed
    {//GEN-HEADEREND:event_btSelectAsMaskActionPerformed
        undo();
        setPaintingTool(new SelectionAsMask(this));
    }//GEN-LAST:event_btSelectAsMaskActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_jTabbedPane1StateChanged
    {//GEN-HEADEREND:event_jTabbedPane1StateChanged
        int oldTab = selectedTab;
        int newTab = jTabbedPane1.getSelectedIndex();
        
        
        
        selectedTab = newTab;
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void btPolyExtractActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btPolyExtractActionPerformed
    {//GEN-HEADEREND:event_btPolyExtractActionPerformed
        setPaintingTool(new ExtractShapeTool());
    }//GEN-LAST:event_btPolyExtractActionPerformed

    private void imageListKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_imageListKeyPressed
    {//GEN-HEADEREND:event_imageListKeyPressed
        int code = evt.getKeyCode();
        int mod = evt.getModifiers();

        if(code == KeyEvent.VK_V && mod == KeyEvent.CTRL_MASK)
        {
            SystemClipboard.paste(this);
        }        
        else if(code == KeyEvent.VK_C && mod == KeyEvent.CTRL_MASK)
        {
            SwingUtilities.invokeLater(new Runnable() 
            {
                @Override
                public void run()
                {
                    copyToClipboard();
                }
            });
        }        
    }//GEN-LAST:event_imageListKeyPressed

    private void btPolyCopyActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btPolyCopyActionPerformed
    {//GEN-HEADEREND:event_btPolyCopyActionPerformed
        setPaintingTool(new CopyShapeTool());
    }//GEN-LAST:event_btPolyCopyActionPerformed

    private void btSpraySetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSpraySetupActionPerformed
        setPaintingTool(new SpraySetupTool());
    }//GEN-LAST:event_btSpraySetupActionPerformed

    private void btSprayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSprayActionPerformed
        setPaintingTool(new SpraycanTool());
    }//GEN-LAST:event_btSprayActionPerformed

    private void brushFromSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brushFromSelectionActionPerformed
        brush.copyFrom(selection.image);
        selection.clear();
        imageView.setOverlay(null);
    }//GEN-LAST:event_brushFromSelectionActionPerformed

    private void brushFromDotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brushFromDotActionPerformed
        brush.mode = Brush.Mode.PLAIN;
    }//GEN-LAST:event_brushFromDotActionPerformed
    
    public void selectRubberbandSelectionButton()
    {
        btSelection.setSelected(true);
        selectRubberbandSelection();
    }

    private void selectRubberbandSelection()
    {
        setPaintingTool(new RubberbandSelection(this));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        IOPluginBroker.loadPlugins();

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TilesetEditor().setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem brushFromDot;
    private javax.swing.JMenuItem brushFromSelection;
    private javax.swing.JMenu brushMenu;
    private javax.swing.JToggleButton btAntiAlias;
    private javax.swing.JButton btEditor;
    private javax.swing.JToggleButton btFill;
    private javax.swing.JToggleButton btFillContour;
    private javax.swing.JToggleButton btFillGradient;
    private javax.swing.JToggleButton btFillOutline;
    private javax.swing.JToggleButton btLine;
    private javax.swing.JToggleButton btOval;
    private javax.swing.JToggleButton btPoint;
    private javax.swing.JToggleButton btPolyCopy;
    private javax.swing.JToggleButton btPolyExtract;
    private javax.swing.JToggleButton btRecolor;
    private javax.swing.JToggleButton btRect;
    private javax.swing.JToggleButton btRoundRect;
    private javax.swing.JToggleButton btSelectAsMask;
    private javax.swing.JToggleButton btSelectClear;
    private javax.swing.JToggleButton btSelectRectangle;
    private javax.swing.JToggleButton btSelection;
    private javax.swing.JToggleButton btShadeBorder;
    private javax.swing.JToggleButton btShadingFill;
    private javax.swing.JToggleButton btSpray;
    private javax.swing.JToggleButton btSpraySetup;
    private javax.swing.JButton btUnrange;
    private javax.swing.JCheckBox cbPaintFilled;
    private javax.swing.JPanel colorToolsPanel;
    private javax.swing.JPanel colorsPanelContainer;
    private javax.swing.JLabel coordField;
    private javax.swing.JLabel coordLabel;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel footField;
    private javax.swing.JLabel footLabel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel idField;
    private javax.swing.JLabel idLabel;
    private javax.swing.JList imageList;
    private javax.swing.JPanel imageListContainer;
    private javax.swing.JScrollPane imageListScroller;
    private javax.swing.JPanel imagePanelContainer;
    private javax.swing.JScrollPane intScroller;
    private javax.swing.JPanel intsContainer;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel leftPanel;
    private javax.swing.JButton moveLeftButton;
    private javax.swing.JButton moveRightButton;
    private javax.swing.JLabel offsetField;
    private javax.swing.JLabel offsetLabel;
    private javax.swing.JPanel paddingPanel;
    private javax.swing.JPanel paintToolsPanel;
    private javax.swing.JPanel pluginToolsPanel;
    private javax.swing.JMenu previewMenu;
    private javax.swing.JTextField rasterField;
    private javax.swing.JPanel selectionToolsPanel;
    private javax.swing.JScrollPane stringScroller;
    private javax.swing.JPanel stringsContainer;
    private javax.swing.JMenuBar theMenuBar;
    private javax.swing.JSplitPane tilesetSplitter;
    private javax.swing.JLabel toleranceLabel;
    private javax.swing.JSpinner toleranceSpinner;
    private javax.swing.ButtonGroup toolButtonGroup;
    private javax.swing.JPanel toolPanel;
    private javax.swing.JList toolPluginList;
    private javax.swing.JMenu transformsMenu;
    private javax.swing.JPanel upperPanel;
    // End of variables declaration//GEN-END:variables

        
    // Hajo: Tools as objects.
    private final static int REPAINT_REFRESH = 6;
    private final static int REPAINT_NOTHING = 7;
        
    private int toolRepaintMode = REPAINT_NOTHING;
    private PaintingTool paintingTool;

    private final Point firstClick = new Point();
    private Color paintColor = Color.WHITE;
    private int shiftSelectedColorIndex = -1;

    private void askResize(boolean smooth)
    {
        int percent = requester.askNumber(this, "<html>Please enter the scaling<br>factor in percent:</html>",
                                50);

        TileDescriptor tld = tileSet.get(currentTile);
        Dimension d = new Dimension(tld.img.getWidth() * percent / 100,
                                    tld.img.getHeight() * percent / 100);
        
        reshape(d, smooth);
    }
    
    private void askReshape(boolean smooth)
    {
        TileDescriptor tld = tileSet.get(currentTile);
        String preset = "" + tld.img.getWidth() + "x" + tld.img.getHeight();

        final Dimension d = requester.askDimension(this, "<html>Please enter the new tile size,<br>" +
                                         "in format WxH:</html>",
                                         preset);

        reshape(d, smooth);
    }


    private void reshape(Dimension size, boolean smooth)
    {
        updateImageData(currentTile);
        saveUndo();

        clearImage(canvas);
        Graphics2D gr2 = canvas.createGraphics();

        Image source = tileSet.get(currentTile).img;

        RenderingHints hints = gr2.getRenderingHints();

        if(smooth)
        {
            gr2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            gr2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        }
        else
        {
            gr2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }

        gr2.drawImage(source,
                      0,
                      0,
                      size.width,
                      size.height,
                      null);

        gr2.setRenderingHints(hints);

        imageClicked(currentTile);
    }

    
    private void startFillmachine(Fillmachine.Filler filler)
    {
        toolRepaintMode = REPAINT_REFRESH;
        Integer tolerance = (Integer)toleranceSpinner.getValue();
        int selectedBackground = colorPalette.getSelectedBackground();
        Color backColor = colorPalette.getColor(selectedBackground);
        Fillmachine fillmachine = new Fillmachine(this, canvas, tolerance*500, backColor);
        fillmachine.setFiller(filler);
        paintingTool = fillmachine;        
    }

    private void copyToClipboard()
    {
        if(currentTile >= 0)
        {
            TileDescriptor tld = tileSet.get(currentTile);
            SystemClipboard.copy(tld.img);
        }        
    }

    private String makeSequenceNumber(int numberWidth, int sequenceNumber) 
    {
        String number = "" + sequenceNumber;
        
        int zeros = numberWidth - number.length();
        
        for(int i=0; i<zeros; i++)
        {
            number = "0" + number;
        }
        
        return number;
    }
    
    private class MouseEventHandler extends MouseAdapter
    {
        private Graphics gr;
        
        @Override
        public void mousePressed(MouseEvent e) 
        {
            // System.err.println("Mouse clicked: " + e);

            firstClick.x = e.getX()/zoomLevel;
            firstClick.y = e.getY()/zoomLevel;
            
            gr = canvas.getGraphics();

            // Hajo: Save data for one undo step
            saveUndo();
            
            final int mods = e.getModifiersEx(); 
            if(0 != (mods & InputEvent.CTRL_DOWN_MASK) &&
               0 != (mods & InputEvent.SHIFT_DOWN_MASK)) 
            {
                System.err.println("Set foot point: " + firstClick);
                
                setFootPoint(firstClick.x, firstClick.y);

                // Don't paint.
                return;
            }
            else if(0 != (mods & InputEvent.CTRL_DOWN_MASK)) 
            {
                final int argb = canvas.getRGB(firstClick.x, firstClick.y);
                paintColor = new Color(argb);
                colorPalette.setColor(colorPalette.getSelectedForeground(), paintColor);
                System.err.println("Pick color: " + paintColor);
                
                // Don't paint.
                return;
            }
            else if(0 != (mods & InputEvent.SHIFT_DOWN_MASK)) 
            {

                final int argb = canvas.getRGB(firstClick.x, firstClick.y);
                final int best = colorPalette.bestColorMatch(argb);

                shiftSelectedColorIndex = colorPalette.getSelectedForeground();
                colorPalette.setSelectedForeground(best);
                paintColor = colorPalette.getColor(best);

                // Don't paint.
                return;
            }
            
            gr.setColor(paintColor);
            paintingTool.firstClick(gr, firstClick.x, firstClick.y);
            imageView.repaint(20);
        }

        @Override
        public void mouseReleased(MouseEvent e)
        {
            imageClicked(currentTile);

            if(paintingTool != null)
            {
                paintingTool.onMouseReleased();
            }
        }

        boolean isValid(int x, int y) {
            return x >= 0 && y >= 0 && x < canvas.getWidth() && y < canvas.getHeight();
        }
     }

    private class MouseMotionHandler extends MouseMotionAdapter
    {
        @Override
        public void mouseMoved(final MouseEvent e)
        {
            final int tileX = e.getX()/zoomLevel;
            final int tileY = e.getY()/zoomLevel;

            coordField.setText("" + tileX + ", " + tileY);

            if(previewFrame.isVisible())
            {
                previewFrame.setCursorPosition(tileX, tileY);
                previewFrame.repaint(200);
            }
            
            final Graphics gr = canvas.getGraphics();
            gr.setColor(paintColor);
            paintingTool.onMouseMoved(gr, tileX, tileY);
        }
        
        @Override
        public void mouseDragged(final MouseEvent e)
        {
            final Graphics gr = canvas.getGraphics();

            final int mods = e.getModifiersEx(); 
            final int newX = e.getX()/zoomLevel;
            final int newY = e.getY()/zoomLevel;

            coordField.setText("" + newX + ", " + newY);
            
            if(0 != (mods & InputEvent.SHIFT_DOWN_MASK)) {

                // Have an underlay ? -> reposition image on drag
                if(backgroundTile != null && currentTile > -1) {
                    final TileDescriptor tld = tileSet.get(currentTile);

                    tld.offX += firstClick.x - newX;
                    tld.offY += firstClick.y - newY;

                    firstClick.x = newX;
                    firstClick.y = newY;

                    updateUnderlayOffset();
                    imageView.repaint(20);

                    if(shiftSelectedColorIndex != colorPalette.getSelectedForeground())
                    {
                        colorPalette.setSelectedForeground(shiftSelectedColorIndex);
                        paintColor = colorPalette.getColor(shiftSelectedColorIndex);
                    }
                }
                
                // Hajo: just drag, don't paint.
                return;
            }
            
            if(toolRepaintMode == REPAINT_REFRESH)
            {
                undo();
            }

            gr.setColor(paintColor);
            paintingTool.paint(gr, newX, newY,
                               0 != (mods & InputEvent.ALT_DOWN_MASK) ||
                               cbPaintFilled.isSelected());
            imageView.repaint(20);
        }
    }

    private class MouseWheelHandler implements MouseWheelListener
    {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            final int u = e.getWheelRotation();
            final int mods = e.getModifiersEx();

            if(0 != (mods & InputEvent.SHIFT_DOWN_MASK))
            {
                Rectangle r = imageView.getVisibleRect();
                r.x += u*3*zoomLevel;
                r.width += u*3*zoomLevel;
                imageView.scrollRectToVisible(r);
            }
            else if(0 != (mods & InputEvent.CTRL_DOWN_MASK))
            {
                Rectangle r = imageView.getVisibleRect();
                r.y += u*3*zoomLevel;
                r.height += u*3*zoomLevel;
                imageView.scrollRectToVisible(r);
            }
            else
            {
                final int oldZoomLevel = zoomLevel;
                
                zoomLevel -= u;
                if(zoomLevel < 1) 
                {
                    zoomLevel = 1;
                }
                imageView.setZoomLevel(zoomLevel);
                
                // Hajo: make scrollpane show the old area again
                // zooming might have moved it away.
                Rectangle r = imageViewScrollPane.getViewport().getViewRect();
                
                // Hajo: exception: only scroll if we are NOT all top-left
                if(r.x > 0 && r.y > 0)
                {
                    r.x = (r.x + r.width / 2) * zoomLevel / oldZoomLevel;
                    r.y = (r.y + r.height / 2) * zoomLevel / oldZoomLevel;

                    r.x = r.x - r.width / 2;
                    r.y = r.y - r.height / 2;

                    imageView.scrollRectToVisible(r);
                }
            }
        }
    }
    
    /**
     * Pan image by given offset.
     * @param dx x offset
     * @param dy y offset
     */
    private void pan(final int dx, final int dy)
    {
        // imageClicked(currentImage);
        
        saveUndo();
        clearImage(canvas);
        canvas.getGraphics().drawImage(undoCanvas, dx, dy, null);

        // tld.xoff += dx;
        // tld.yoff += dy;

        imageClicked(currentTile);
        repaint();
    }

    public void setPaintingTool(PaintingTool paintingTool)
    {
        this.paintingTool = paintingTool;
        toolRepaintMode = paintingTool.getRefreshMode();
        paintingTool.setEditor(this);
        paintingTool.setCanvas(canvas);        
    }

    private void showAnimation()
    {
        if(previewFrame != null)
        {
            previewFrame.setVisible(false);
        }
        
        String [] parts;
                
        do
        {
            animationString = 
                    requester.askString(this, 
                        "Start, count, delay", animationString);
        
            parts = animationString.split(",");
        }
        while(parts.length != 3);
                
        int start = Integer.parseInt(parts[0].trim());
        int count = Integer.parseInt(parts[1].trim());
        int delay = Integer.parseInt(parts[2].trim());
        
        previewFrame = new AnimationPreview(tileSet, start, count, delay);
        previewFrame.setVisible(true);
    }
}
