package TTT;
import TTT.Deadline;
import java.util.*;

public class Player {

    /**
     * Performs a move
     *
     * @param gameState the current state of the board
     * @param deadline time before which we must have returned
     * @return the next state the board is in after our move
     */
    int idPlayer;

    public GameState play(final GameState gameState, final Deadline deadline) {
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);
        idPlayer = gameState.getNextPlayer() == 1 ? 2 : 1;
        /**
         * Here you should write your algorithms to get the best next move, i.e.
         * the best next state. This skeleton returns a random move instead.
         */
        //Random random = new Random();
        if (false){//idPlayer == Constants.CELL_O) {
            System.err.println(" RANDOM CHOICE  PLAYER " + playerName(idPlayer));
            Random random = new Random();

            return nextStates.elementAt(random.nextInt(nextStates.size()));
        }
        System.err.println(" MINIMAX CHOICE  PLAYER " + playerName(idPlayer));

        int indexBestNewGameState = 0;
        int bestPossible = Integer.MIN_VALUE;
        int currentIndex = 0;
        boolean myTurnToPlay = false;
        for (GameState newGameState : nextStates) {
            if (deadline.timeUntil() == 1) {
                break;
            }
            int res = minMaxAlgo(newGameState, 5, deadline, myTurnToPlay);
            if (res > bestPossible) {
                bestPossible = res;
                indexBestNewGameState = currentIndex;
            }
            currentIndex++;

        }
        return nextStates.get(indexBestNewGameState);
    }

    public int gama(int player, GameState gameState) {
        if (gameState.isOWin()) {
            if (idPlayer == Constants.CELL_O) {
                return Integer.MAX_VALUE;
            } else if (idPlayer == Constants.CELL_X) {
                return Integer.MIN_VALUE;
            }
        } else if (gameState.isXWin()) {
            if (idPlayer == Constants.CELL_X) {
                return Integer.MAX_VALUE;
            } else if (idPlayer == Constants.CELL_O) {
                return Integer.MIN_VALUE;
            }
        } else {
            int nbMarksRows = 0;
            int nbMarksCol = 0;
            int nbMarksDiag = 0;
            for (int i = 0; i < 4; i++) {

                for (int j = 0; j < 4; j++) {
                    if (gameState.at(i, j) == idPlayer) {
                        nbMarksRows++;
                    }
                }
                for (int j = 0; j < 4; j++) {
                    if (gameState.at(j, i) == idPlayer) {
                        nbMarksCol++;
                    }
                }

                if (gameState.at(i, i) == idPlayer) {
                    nbMarksDiag++;
                }

            }
            if (gameState.at(0, 3) == idPlayer) {
                nbMarksDiag++;
            }
            if (gameState.at(1, 2) == idPlayer) {
                nbMarksDiag++;
            }
            if (gameState.at(2, 1) == idPlayer) {
                nbMarksDiag++;
            }
            if (gameState.at(3, 0) == idPlayer) {
                nbMarksDiag++;
            }

            return nbMarksCol + nbMarksDiag + nbMarksRows;
        }
        return 0;
    }

    public int minMaxAlgo(GameState currentGameState, int maxDepth, Deadline deadline, boolean MyTurnToPlay) {

        Vector<GameState> nextStates = new Vector<GameState>();
        currentGameState.findPossibleMoves(nextStates);
        maxDepth--;
        if (maxDepth == 0 || currentGameState.isEOG() || deadline.timeUntil() < 2) {
            // Must return the heuristic value of the current state
            return gama(currentGameState.getNextPlayer(), currentGameState);
        }

        if (MyTurnToPlay) {
            int bestPossible = Integer.MIN_VALUE;
            for (GameState newGameSate : nextStates) {
                int res = minMaxAlgo(newGameSate, maxDepth, deadline, !MyTurnToPlay);
                if (res > bestPossible) {
                    bestPossible = res;
                }
            }
            return bestPossible;

        } else {
            int bestPossible = Integer.MAX_VALUE;
            for (GameState newGameSate : nextStates) {
                int res = minMaxAlgo(newGameSate, maxDepth, deadline, !MyTurnToPlay);
                if (res < bestPossible) {
                    bestPossible = res;
                }
            }
            return bestPossible;
        }

    }

    public String playerName(int i) {
        return i == 1 ? "X" : "O";
    }

}
