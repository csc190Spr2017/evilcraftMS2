/*
 * Copyright (C) 2019 csc190
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package FXDevices;

import BridgePattern.ICanvasDevice;
import BridgePattern.IGameEngine;
import BridgePattern.IStopWatch;

import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;

/**
 * FXVersion Implementation of ICanvasDevice
 *
 * @author csc190
 */
public class FXCanvasDevice implements ICanvasDevice {

    //--------------------------------------
    //data members
    //--------------------------------------
    protected Canvas canvas;
    protected Hashtable<String, Image> map = new Hashtable();

    //--------------------------------------
    //methods
    //--------------------------------------
    protected Image getImage(String picpath) {
        if (!map.containsKey(picpath)) {
            //Somehow had to chop off the "resources part"
            String path2 = picpath.substring(picpath.indexOf("/") + 1);
            InputStream is = getClass().getClassLoader().getResourceAsStream(path2);

            Image img = new Image(is);
            map.put(picpath, img);
        }
        return map.get(picpath);
    }

    public FXCanvasDevice(Canvas canvas) {
        this.canvas = canvas;
        canvas.setCache(false);
        //canvas.setCacheHint(CacheHint.SPEED);

    }

    protected GraphicsContext mygc = null;

    @Override
    public void drawImg(String imgPath, int x, int y, int width, int height, int degree) {
        Image img = getImage(imgPath);
        GraphicsContext gc = mygc != null ? mygc : this.canvas.getGraphicsContext2D();
        mygc = gc;

        if (degree > 0) {
            gc.save();
            Rotate r = new Rotate(degree, x + width / 2, y + height / 2);
            gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());
            gc.drawImage(img, x, y, width, height);
            gc.restore();
        } else {
            gc.drawImage(img, x, y, width, height);
        }

    }

    @Override
    public int getWidth() {
        return (int) this.canvas.getWidth();
    }

    @Override
    public int getHeight() {
        return (int) this.canvas.getHeight();
    }

    @Override
    public IStopWatch createStopWatch(String name) {
        FXStopWatch watch = new FXStopWatch(name);
        return watch;
    }

    protected int x1, x2, y1, y2;
    protected boolean bRightDown = false;

    @Override
    public void setupEventHandler(IGameEngine gameEngine) {
        ICanvasDevice me = this;
        
        //2. set up mouse drag event
        this.canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                x1 = (int) event.getX();
                y1 = (int) event.getY();
                bRightDown = event.isSecondaryButtonDown();
            }
        });

        this.canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                x2 = (int) event.getX();
                y2 = (int) event.getY();
                if (x1 != x2 || y1 != y2) {
                    gameEngine.onRegionSelected(me, x1, y1, x2, y2);
                }else{
                    if(bRightDown){
                        gameEngine.onRightClick(me, x1, y1);
                    }else{
                        gameEngine.onLeftClick(me, x1, y1);
                    }
                }
            }
        });
    }

    @Override
    public void clear() {
        this.canvas.getGraphicsContext2D().clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
    }

}
