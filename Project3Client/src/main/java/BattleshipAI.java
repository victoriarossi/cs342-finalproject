//import java.util.Random;
//
//public class BattleshipAI {
//    private int gridSize = 10; // assuming a 10x10 grid
//    private boolean[][] grid = new boolean[gridSize][gridSize]; // track placed ships
//    public GuiClient theGui = new GuiClient();
//    // Randomly place ships on the grid
//    public void placeShips() {
//        int[] shipSizes = {5, 4, 3, 3, 2}; // sizes of the ships
//        Random random = new Random();
//
//        for (int size : shipSizes) {
//            boolean placed = false;
//            while (!placed) {
//                int x = random.nextInt(gridSize);
//                int y = random.nextInt(gridSize);
//                boolean horizontal = random.nextBoolean();
//
////                if (canPlaceShip(x, y, size, horizontal)) {
////                    for (int i = 0; i < size; i++) {
////                        if (horizontal) {
////                            grid[x][y + i] = true; // Place horizontally
////                        } else {
////                            grid[x + i][y] = true; // Place vertically
////                        }
////                    }
////                    placed = true;
////                }
//            }
//        }
//
//}
