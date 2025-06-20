# Simple Flowchart - A Basic Android Flowchart Application

This is a demonstration-focused Android application developed as a final course project. The goal was to create a simple flowchart maker that mimics the core interactive experience of tools like `draw.io`.

The entire project is built from scratch using **Kotlin** and **Android Studio**, relying solely on the native Android Custom View system without any third-party charting or diagramming libraries.

[English](README.md) \ [中文](README_CN.md)

---

## ✨ Features

*   **Grid Canvas**: An infinite canvas with a grid background that serves as the main workspace.
*   **Dynamic Shape Creation**: Tap the Floating Action Button (FAB) to open a dialog and add new shapes like "Rectangle" and "Circle" to the canvas.
*   **Single-Finger Drag & Drop**: Effortlessly move any shape around the canvas with a single finger to arrange your layout.
*   **Two-Finger Pinch-to-Zoom**: Dynamically resize any shape by using a two-finger pinch gesture.
*   **Long-Press to Connect**: Initiate "Connection Mode" by performing a long press on any shape.
*   **Real-time Connection Preview**: Once in connection mode, a live preview line is drawn from the starting shape to your finger's current position.
*   **Automatic Orthogonal Connectors**: When you lift your finger over another shape, a clean, right-angled polyline is automatically drawn between the centers of the two shapes.
*   **Responsive Connectors**: When a connected shape is moved, the connector line updates in real-time to maintain the link.

---

## 📸 Screenshots & Demo

![App Demo GIF](https://github.com/DSDHX/SimpleFlowchartMobile/blob/main/readme_file/demoGIF.gif)

| Grid Canvas | Complete a Connection | Drag & Scale |
| :---: | :---: | :---: |
| ![Screenshot of Grid Canvas](https://github.com/DSDHX/SimpleFlowchartMobile/blob/main/readme_file/BackGround.png) | ![Screenshot of a completed connection](https://github.com/DSDHX/SimpleFlowchartMobile/blob/main/readme_file/ThreeShape.png) | ![Screenshot of dragging and scaling](https://github.com/DSDHX/SimpleFlowchartMobile/blob/main/readme_file/SizeChange.png) |

---

## 🛠️ Tech Stack & Tools

*   **Language**: [Kotlin](https://kotlinlang.org/)
*   **IDE**: [Android Studio](https://developer.android.com/studio)
*   **Core Technologies**:
    *   **Custom View**: The entire canvas is a single custom class extending `android.view.View`. All drawing logic (grid, shapes, lines) is handled within the `onDraw(canvas: Canvas)` method.
    *   **Touch Event Handling (`onTouchEvent`)**: Finely-tuned handling of `ACTION_DOWN`, `ACTION_MOVE`, and `ACTION_UP` to manage and differentiate user interactions.
    *   **Gesture Detectors**:
        *   `ScaleGestureDetector`: To natively handle pinch-to-zoom gestures.
        *   `GestureDetector`: To detect long-press gestures for initiating the connection mode.
    *   **View Binding**: For type-safe and concise access to views defined in the XML layout.
    *   **Material Components**: Use of `FloatingActionButton` and `AlertDialog` for a modern and consistent UI.

---

## 🔮 Potential Improvements

*   **More Shape Types**: Add support for diamonds, parallelograms (for I/O), etc.
*   **Text inside Shapes**: Allow users to double-tap a shape to add and edit text.
*   **Save/Load Project**: Serialize the canvas state (all shapes and connections, e.g., to JSON) and save it to a file.
*   **Grid Snapping**: Automatically snap shapes to the nearest grid lines when dragging.
*   **Customizable Connectors**: Allow users to drag the turning points of a line or change its style.
