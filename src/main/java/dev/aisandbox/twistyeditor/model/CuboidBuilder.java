package dev.aisandbox.twistyeditor.model;

import dev.aisandbox.twistyeditor.PuzzleUtil;
import dev.aisandbox.twistyeditor.model.shapes.ShapeEnum;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CuboidBuilder {

  private int scale;
  private static final int gap = 4;

  private final ObservableList<Cell> cellObservableList;
  private final ObservableList<Move> moveObservableList;

  private final List<Cell> left = new ArrayList<>();
  private final List<Cell> right = new ArrayList<>();
  private final List<Cell> top = new ArrayList<>();
  private final List<Cell> bottom = new ArrayList<>();
  private final List<Cell> front = new ArrayList<>();
  private final List<Cell> back = new ArrayList<>();

  private final int width;
  private final int height;
  private final int depth;

  public CuboidBuilder(
      ObservableList<Cell> cellObservableList,
      ObservableList<Move> moveObservableList,
      int width,
      int height,
      int depth) {
    this.cellObservableList = cellObservableList;
    this.moveObservableList = moveObservableList;
    this.width = width;
    this.height = height;
    this.depth = depth;
  }

  public void createCuboid() throws IOException {
    log.info("Creating cuboid {}x{}x{}", width, height, depth);
    // work out scale
    int vscale = Puzzle.HEIGHT / (height * 6 + gap * 2);
    int hscale = Puzzle.WIDTH / (width * 4 + depth * 4 + gap * 3);
    scale = Math.min(vscale, hscale) + 1;
    log.info("Calculating scale, vscale={}, hscale={}, scale={}", vscale, hscale, scale);
    // create white (top) grid
    top.addAll(createGrid(0, 0, width, depth, ColourEnum.WHITE));
    // create orange (left) grid
    left.addAll(
        createGrid(
            -depth * scale * 2 - gap, depth * scale * 2 + gap, depth, height, ColourEnum.ORANGE));
    // create green (front) grid
    front.addAll(createGrid(0, depth * scale * 2 + gap, width, height, ColourEnum.GREEN));
    // create red (right) grid
    right.addAll(
        createGrid(
            width * scale * 2 + gap, depth * scale * 2 + gap, depth, height, ColourEnum.RED));
    // create blue (back) grid
    back.addAll(
        createGrid(
            (width + depth) * scale * 2 + gap * 2,
            depth * scale * 2 + gap,
            width,
            height,
            ColourEnum.BLUE));
    // create yellow (bottom) grid
    bottom.addAll(
        createGrid(0, (depth + height) * scale * 2 + gap * 2, width, depth, ColourEnum.YELLOW));
    // add all cells to puzzle
    cellObservableList.addAll(top);
    cellObservableList.addAll(left);
    cellObservableList.addAll(front);
    cellObservableList.addAll(right);
    cellObservableList.addAll(back);
    cellObservableList.addAll(bottom);
    // create moves
    Move move;
    CuboidMoveIcon icon;
    if (width == height) {
      // we can have F,F',B,B',z,z' moves
      for (int deep = 1; deep < depth; deep++) {
        // create F moves
        move = new Move();
        move.setName(getMoveName(deep, 'F', 1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('F',false);
        icon.fillFrontFace();
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'F', 1)));
        log.info("Generating {}", move.getName());
        move.getLoops().addAll(faceTurn(front, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(frontSideTurn(layer));
        }
        moveObservableList.add(move);
        // create F' move
        move = new Move();
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('F',true);
        icon.fillFrontFace();
        move.setName(getMoveName(deep, 'F', -1));
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'F', -1)));
        log.info("Generating {}", move.getName());
        move.getLoops().addAll(faceReverseTurn(front, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(frontSideReverseTurn(layer));
        }
        moveObservableList.add(move);
        // B moves
        move = new Move();
        move.setName(getMoveName(deep, 'B', 1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('B',false);
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'B', 1)));
        move.getLoops().addAll(faceTurn(back, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(frontSideReverseTurn(depth - layer + 1));
        }
        moveObservableList.addAll(move);
        // B' moves
        move = new Move();
        move.setName(getMoveName(deep, 'B', -1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('B',true);
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'B', -1)));
        move.getLoops().addAll(faceReverseTurn(back, width, height));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(frontSideTurn(depth - layer + 1));
        }
        moveObservableList.addAll(move);
      }
      // z move
      move = new Move();
      move.setName(getMoveName(0, 'F', 1));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('F',false);
      icon.fillFrontFace();
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'F', 1)));
      move.getLoops().addAll(faceTurn(front, width, height));
      move.getLoops().addAll(faceReverseTurn(back, width, height));
      for (int layer = 1; layer <= depth; layer++) {
        move.getLoops().addAll(frontSideTurn(layer));
      }
      move.setCost(0);
      moveObservableList.add(move);
      // z' move
      move = new Move();
      move.setName(getMoveName(0, 'F', -1));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('F',true);
      icon.fillFrontFace();
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'F', -1)));
      move.getLoops().addAll(faceReverseTurn(front, width, height));
      move.getLoops().addAll(faceTurn(back, width, height));
      for (int layer = 1; layer <= depth; layer++) {
        move.getLoops().addAll(frontSideReverseTurn(layer));
      }
      move.setCost(0);
      moveObservableList.add(move);
    }
    if (width == depth) {
      // we can have U,U',D,D',y,y'
      for (int deep = 1; deep < height; deep++) {
        // U Move
        move = new Move();
        move.setName(getMoveName(deep, 'U', 1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('U',false);
        move.getLoops().addAll(faceTurn(top, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(topSideTurn(layer));
          for (int x=0;x<width;x++) {
            icon.fillFrontFace(x,layer-1);
          }
        }
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'U', 1)));
        moveObservableList.add(move);
        // U' move
        move = new Move();
        move.setName(getMoveName(deep, 'U', -1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('U',true);
        move.getLoops().addAll(faceReverseTurn(top, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(topSideReverseTurn(layer));
          for (int x=0;x<width;x++) {
            icon.fillFrontFace(x,layer-1);
          }
        }
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'U', -1)));
        moveObservableList.add(move);
        // D move
        move = new Move();
        move.setName(getMoveName(deep, 'D', 1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('D',false);
        move.getLoops().addAll(faceTurn(bottom, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(topSideReverseTurn(height - layer + 1));
          for (int x = 0; x < width; x++) {
            icon.fillFrontFace(x, height - layer);
          }
        }
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'D', 1)));
        moveObservableList.add(move);
        // D' move
        move = new Move();
        move.setName(getMoveName(deep, 'D', -1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('D',true);
        move.getLoops().addAll(faceReverseTurn(bottom, width, depth));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(topSideTurn(height - layer + 1));
          for (int x = 0; x < width; x++) {
            icon.fillFrontFace(x, height - layer);
          }
        }
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'D', -1)));
        moveObservableList.add(move);
      }
      // y move
      move = new Move();
      move.setName(getMoveName(0, 'U', 1));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('U',false);
      icon.fillFrontFace();
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'U', 1)));
      move.getLoops().addAll(faceTurn(top, width, depth));
      move.getLoops().addAll(faceReverseTurn(bottom, width, depth));
      for (int layer = 1; layer <= height; layer++) {
        move.getLoops().addAll(topSideTurn(layer));
      }
      move.setCost(0);
      moveObservableList.add(move);
      // y' move
      move = new Move();
      move.setName(getMoveName(0, 'U', -1));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('U',true);
      icon.fillFrontFace();
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'U', -1)));
      move.getLoops().addAll(faceReverseTurn(top, width, depth));
      move.getLoops().addAll(faceTurn(bottom, width, depth));
      for (int layer = 1; layer <= height; layer++) {
        move.getLoops().addAll(topSideReverseTurn(layer));
      }
      move.setCost(0);
      moveObservableList.add(move);
    }
    if (depth == height) {
      // we can have R,R',L,L',z,z'
      for (int deep = 1; deep < width; deep++) {
        // R
        move = new Move();
        move.setName(getMoveName(deep, 'R', 1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('R',false);
        move.getLoops().addAll(faceTurn(right, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(rightSideTurn(layer));
          for (int y=0;y<height;y++) {
            icon.fillFrontFace(width-layer,y);
          }
        }
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'R', 1)));
        moveObservableList.add(move);
        // R'
        move = new Move();
        move.setName(getMoveName(deep, 'R', -1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('R',true);
        move.getLoops().addAll(faceReverseTurn(right, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(rightSideReverseTurn(layer));
          for (int y=0;y<height;y++) {
            icon.fillFrontFace(width-layer,y);
          }
        }
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'R', -1)));
        moveObservableList.add(move);
        // L
        move = new Move();
        move.setName(getMoveName(deep, 'L', 1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('L',false);
        move.getLoops().addAll(faceTurn(left, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(rightSideReverseTurn(width - layer + 1));
          for (int y=0;y<height;y++) {
            icon.fillFrontFace(layer-1,y);
          }
        }
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'L', 1)));
        moveObservableList.add(move);
        // L'
        move = new Move();
        move.setName(getMoveName(deep, 'L', -1));
        icon = new CuboidMoveIcon(width, height, depth);
        icon.setRotation('L',true);
        move.getLoops().addAll(faceReverseTurn(left, depth, height));
        for (int layer = 1; layer <= deep; layer++) {
          move.getLoops().addAll(rightSideTurn(width - layer + 1));
          for (int y=0;y<height;y++) {
            icon.fillFrontFace(layer-1,y);
          }
        }
        move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'L', -1)));
        moveObservableList.add(move);
      }
      // z
      move = new Move();
      move.setName(getMoveName(0, 'R', 1));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.fillFrontFace();
      icon.setRotation('R',false);
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'R', 1)));
      move.getLoops().addAll(faceTurn(right, depth, height));
      move.getLoops().addAll(faceReverseTurn(left, depth, height));
      for (int layer = 1; layer <= width; layer++) {
        move.getLoops().addAll(rightSideTurn(layer));
      }
      move.setCost(0);
      moveObservableList.add(move);
      // z'
      move = new Move();
      move.setName(getMoveName(0, 'R', -1));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('R',true);
      icon.fillFrontFace();
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'R', -1)));
      move.getLoops().addAll(faceReverseTurn(right, depth, height));
      move.getLoops().addAll(faceTurn(left, depth, height));
      for (int layer = 1; layer <= width; layer++) {
        move.getLoops().addAll(rightSideReverseTurn(layer));
      }
      move.setCost(0);
      moveObservableList.add(move);
    }
    // we can always have double turns
    for (int deep = 1; deep < depth; deep++) {
      // F2 moves
      move = new Move();
      move.setName(getMoveName(deep, 'F', 2));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('F',false);
      icon.fillFrontFace();
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'F', 2)));
      move.getLoops().addAll(faceDoubleTurn(front, width, height));
      for (int layer = 1; layer <= deep; layer++) {
        move.getLoops().addAll(frontSideDoubleTurn(layer));
      }
      moveObservableList.add(move);
      // B2
      move = new Move();
      move.setName(getMoveName(deep, 'B', 2));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('B',false);
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'B', 2)));
      move.getLoops().addAll(faceDoubleTurn(back, width, height));
      for (int layer = 1; layer <= deep; layer++) {
        move.getLoops().addAll(frontSideDoubleTurn(depth - layer + 1));
      }
      moveObservableList.add(move);
    }
    // z2
    move = new Move();
    move.setName(getMoveName(0, 'F', 2));
    icon = new CuboidMoveIcon(width, height, depth);
    icon.fillFrontFace();
    icon.setRotation('F',false);
    move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'F', 2)));
    move.getLoops().addAll(faceDoubleTurn(front, width, height));
    move.getLoops().addAll(faceDoubleTurn(back, width, height));
    for (int layer = 1; layer <= depth; layer++) {
      move.getLoops().addAll(frontSideDoubleTurn(layer));
    }
    move.setCost(0);
    moveObservableList.add(move);
    for (int deep = 1; deep < height; deep++) {
      // U2
      move = new Move();
      move.setName(getMoveName(deep, 'U', 2));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('U',false);
      move.getLoops().addAll(faceDoubleTurn(top, width, depth));
      for (int layer = 1; layer <= deep; layer++) {
        move.getLoops().addAll(topSideDoubleTurn(layer));
        for (int x=0;x<width;x++) {
          icon.fillFrontFace(x,layer-1);
        }
      }
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'U', 2)));
      moveObservableList.add(move);
      //  D2
      move = new Move();
      move.setName(getMoveName(deep, 'D', 2));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('D',false);
      move.getLoops().addAll(faceDoubleTurn(bottom, width, depth));
      for (int layer = 1; layer <= deep; layer++) {
        move.getLoops().addAll(topSideDoubleTurn(height - layer + 1));
        for (int x=0;x<width;x++) {
          icon.fillFrontFace(x,height-layer);
        }
      }
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'D', 2)));
      moveObservableList.add(move);
    }
    //  y2
    move = new Move();
    move.setName(getMoveName(0, 'U', 2));
    icon = new CuboidMoveIcon(width, height, depth);
    icon.setRotation('U',false);
    icon.fillFrontFace();
    move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'U', 2)));
    move.getLoops().addAll(faceDoubleTurn(top, width, depth));
    move.getLoops().addAll(faceDoubleTurn(bottom, width, depth));
    for (int layer = 1; layer <= height; layer++) {
      move.getLoops().addAll(topSideDoubleTurn(layer));
    }
    move.setCost(0);
    moveObservableList.add(move);
    for (int deep = 1; deep < width; deep++) {
      //  R2
      move = new Move();
      move.setName(getMoveName(deep, 'R', 2));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('R',false);
      move.getLoops().addAll(faceDoubleTurn(right, depth, height));
      for (int layer = 1; layer <= deep; layer++) {
        move.getLoops().addAll(rightSideDoubleTurn(layer));
        for (int y=0;y<height;y++) {
          icon.fillFrontFace(width-layer,y);
        }
      }
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'R', 2)));
      moveObservableList.add(move);
      //  L2
      move = new Move();
      move.setName(getMoveName(deep, 'L', 2));
      icon = new CuboidMoveIcon(width, height, depth);
      icon.setRotation('L',false);
      move.getLoops().addAll(faceDoubleTurn(left, depth, height));
      for (int layer = 1; layer <= deep; layer++) {
        move.getLoops().addAll(rightSideDoubleTurn(width - layer + 1));
        for (int y=0;y<height;y++) {
          icon.fillFrontFace(layer-1,y);
        }
      }
      move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(deep, 'L', 2)));
      moveObservableList.add(move);
    }
    // x2
    move = new Move();
    move.setName(getMoveName(0, 'R', 2));
    icon = new CuboidMoveIcon(width, height, depth);
    icon.setRotation('R',false);
    icon.fillFrontFace();
    move.setImageIcon(PuzzleUtil.getMoveIcon(icon.getImage(), getMoveName(0, 'R', 2)));
    move.getLoops().addAll(faceDoubleTurn(right, depth, height));
    move.getLoops().addAll(faceDoubleTurn(left, depth, height));
    for (int layer = 1; layer <= width; layer++) {
      move.getLoops().addAll(rightSideDoubleTurn(layer));
    }
    move.setCost(0);
    moveObservableList.add(move);
  }

  /**
   * Create the move name, if depth<1 then assume a cube rotation
   *
   * @param depth the depth of the turn 0 for all layers
   * @param face the face to turn
   * @param quarterTurns the number of quarter turns
   * @return
   */
  public static String getMoveName(int depth, char face, int quarterTurns) {
    StringBuilder result = new StringBuilder();
    // outer block moves
    if (depth > 2) {
      result.append(depth);
    }
    // face
    if (depth > 0) {
      result.append(face);
    } else {
      switch (face) {
        case 'R':
          result.append("x");
          break;
        case 'U':
          result.append("y");
          break;
        case 'F':
          result.append("z");
          break;
        default:
          result.append("?");
      }
    }
    // outer block move
    if (depth > 1) {
      result.append("w");
    }
    // rotations
    if (quarterTurns == 2) {
      result.append("2");
    } else if (quarterTurns == -1) {
      result.append("'");
    }
    return result.toString();
  }

  /**
   * Create the move name, if depth<1 then assume a cube rotation
   *
   * @param depth the depth of the turn 0 for all layers
   * @param face the face to turn
   * @param quarterTurns the number of quarter turns
   * @return
   */
  @Deprecated
  public BufferedImage getMoveIcon(int depth, char face, int quarterTurns) {
    BufferedImage image = PuzzleUtil.getMoveIcon(getMoveName(depth, face, quarterTurns));
    return image;
  }

  private List<Cell> createGrid(int x, int y, int w, int h, ColourEnum colour) {
    List<Cell> cells = new ArrayList<>();
    for (int dy = 0; dy < h; dy++) {
      for (int dx = 0; dx < w; dx++) {
        Cell c = new Cell();
        c.setShape(ShapeEnum.SQUARE);
        c.setColour(colour);
        c.setLocationX(x + dx * scale * 2);
        c.setLocationY(y + dy * scale * 2);
        c.setScale(scale);
        c.setRotation(0);
        cells.add(c);
      }
    }
    return cells;
  }

  private List<Loop> frontSideTurn(int layer) {
    if (width != height) {
      throw new IllegalStateException();
    }
    List<Loop> result = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      Loop loop = new Loop();
      loop.getCells().add(top.get(i + width * (depth - layer)));
      loop.getCells().add(right.get(layer - 1 + i * depth));
      loop.getCells().add(bottom.get(width - i - 1 + width * (layer - 1)));
      loop.getCells().add(left.get(depth - layer + (height - i - 1) * depth));
      result.add(loop);
    }
    return result;
  }

  private List<Loop> topSideTurn(int layer) {
    if (width != depth) {
      throw new IllegalStateException();
    }
    List<Loop> result = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      Loop loop = new Loop();
      loop.getCells().add(front.get(i + (layer - 1) * width));
      loop.getCells().add(left.get(i + (layer - 1) * width));
      loop.getCells().add(back.get(i + (layer - 1) * width));
      loop.getCells().add(right.get(i + (layer - 1) * depth));
      result.add(loop);
    }
    return result;
  }

  private List<Loop> rightSideTurn(int layer) {
    if (height != depth) {
      throw new IllegalStateException();
    }
    List<Loop> result = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      Loop loop = new Loop();
      loop.getCells().add(front.get(width - layer + i * width));
      loop.getCells().add(top.get(width - layer + i * width));
      loop.getCells().add(back.get(layer - 1 + (height - i - 1) * width));
      loop.getCells().add(bottom.get(width - layer + i * width));
      result.add(loop);
    }
    return result;
  }

  private List<Loop> frontSideReverseTurn(int layer) {
    List<Loop> result = frontSideTurn(layer);
    for (Loop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  private List<Loop> topSideReverseTurn(int layer) {
    List<Loop> result = topSideTurn(layer);
    for (Loop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  private List<Loop> rightSideReverseTurn(int layer) {
    List<Loop> result = rightSideTurn(layer);
    for (Loop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  private List<Loop> frontSideDoubleTurn(int layer) {
    List<Loop> result = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      Loop l = new Loop();
      l.getCells().add(top.get(i + (depth - layer) * width));
      l.getCells().add(bottom.get(width - i - 1 + (layer - 1) * width));
      result.add(l);
    }
    for (int i = 0; i < height; i++) {
      Loop l = new Loop();
      l.getCells().add(right.get(layer - 1 + i * depth));
      l.getCells().add(left.get(depth - layer + (height - i - 1) * depth));
      result.add(l);
    }
    return result;
  }

  private List<Loop> topSideDoubleTurn(int layer) {
    List<Loop> result = new ArrayList<>();
    for (int i = 0; i < width; i++) {
      Loop l = new Loop();
      l.getCells().add(front.get(i + (layer - 1) * width));
      l.getCells().add(back.get(i + (layer - 1) * width));
      result.add(l);
    }
    for (int i = 0; i < depth; i++) {
      Loop l = new Loop();
      l.getCells().add(right.get(i + (layer - 1) * depth));
      l.getCells().add(left.get(i + (layer - 1) * depth));
      result.add(l);
    }
    return result;
  }

  private List<Loop> rightSideDoubleTurn(int layer) {
    List<Loop> result = new ArrayList<>();
    for (int i = 0; i < height; i++) {
      Loop l = new Loop();
      l.getCells().add(front.get(i * width + width - layer));
      l.getCells().add(back.get((height - i - 1) * width + layer - 1));
      result.add(l);
    }
    for (int i = 0; i < depth; i++) {
      Loop l = new Loop();
      l.getCells().add(top.get(i * width + width - layer));
      l.getCells().add(bottom.get(i * width + width - layer));
      result.add(l);
    }
    return result;
  }

  private List<Loop> faceTurn(List<Cell> face, int width, int height) {
    List<Loop> result = new ArrayList<>();
    for (int dx = 0; dx < divRoundUp(width, 2); dx++) {
      for (int dy = 0; dy < height / 2; dy++) {
        Loop loop = new Loop();
        loop.getCells().add(face.get(dx + dy * width));
        loop.getCells().add(face.get((width - dy - 1) + width * dx));
        loop.getCells().add(face.get(width - dx - 1 + width * (height - dy - 1)));
        loop.getCells().add(face.get(dy + width * (height - dx - 1)));
        result.add(loop);
      }
    }
    return result;
  }

  private List<Loop> faceReverseTurn(List<Cell> face, int width, int height) {
    List<Loop> result = faceTurn(face, width, height);
    for (Loop loop : result) {
      Collections.reverse(loop.getCells());
    }
    return result;
  }

  private List<Loop> faceDoubleTurn(List<Cell> face, int width, int height) {
    List<Loop> result = new ArrayList<>();
    for (int dx = 0; dx < width; dx++) {
      for (int dy = 0; dy < height / 2; dy++) {
        Loop loop = new Loop();
        loop.getCells().add(face.get(dx + dy * width));
        loop.getCells().add(face.get(width - dx - 1 + width * (height - dy - 1)));
        result.add(loop);
      }
    }
    // special case for odd numbered heights
    if (height % 2 == 1) {
      int dy = divRoundUp(height, 2) - 1;
      for (int dx = 0; dx < width / 2; dx++) {
        Loop loop = new Loop();
        loop.getCells().add(face.get(dx + dy * width));
        loop.getCells().add(face.get(width - dx - 1 + dy * width));
        result.add(loop);
      }
    }
    return result;
  }

  public static int divRoundUp(int num, int divisor) {
    return (num + divisor - 1) / divisor;
  }
}
