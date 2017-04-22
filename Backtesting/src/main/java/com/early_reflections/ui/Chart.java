package com.early_reflections.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.collections.ListChangeListener;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;

/**
 * This is a more performant Chart that is a periodically updating itself.
 */
public class Chart extends AreaChart<Number, Number> {

    private boolean isRendering=false;
    private UpdateHandler updateHandler;
    private Timeline periodicChartUpdater;

    public Chart(@NamedArg("xAxis") Axis<Number> xAxis, @NamedArg("yAxis") Axis<Number> yAxis) {
        super(xAxis, yAxis);
    }

    void startPeriodicUpdate(UpdateHandler updateHandler){
        this.updateHandler = updateHandler;
        // Updating the chart periodically after some time is much more performant than updating on each new data
         periodicChartUpdater = new Timeline(new KeyFrame(Duration.millis(100), event -> {

            if(isRendering){
                System.out.println("is rendering");
                return;
            }
            isRendering = true;
            boolean hasChanged = updateHandler.update();
            if(!hasChanged){
                isRendering = false;
            }
        }));
        periodicChartUpdater.setCycleCount(Timeline.INDEFINITE);
        periodicChartUpdater.play();
    }

    public void stopUpdating(){
        periodicChartUpdater.stop();
        Platform.runLater(() -> {
            updateHandler.update();
        });
    }


    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        isRendering=false;
        System.out.println("layoutPlotChildren");
    }

    public interface UpdateHandler{

        /**
         * Updating the chart series should be done in this method.
         * @return True if the data has changed otherwise false
         */
        boolean update();
    }

}
