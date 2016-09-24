
import java.util.*;
import sun.misc.VM;

public class Player {

    /**
     * Performs a move
     *
     * @param gameState the current state of the board
     * @param deadline time before which we must have returned
     * @return the next state the board is in after our move
     */
    int idPlayer;
    int depthInit = 3;
    HashMap<Integer, Integer> valuesAlphaBeta;

    public GameState play(final GameState gameState, final Deadline deadline) {
        valuesAlphaBeta = new HashMap<>();
        idPlayer = gameState.getNextPlayer() == 1 ? 2 : 1;
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        /*
        Random random = new Random();
        return nextStates.elementAt(random.nextInt(nextStates.size()));
         */
        int v;
        int bestIndex = 0;
        int index = 0;
        int beta = Integer.MAX_VALUE;
        int alpha = Integer.MIN_VALUE;
        int depth = 4;
        boolean myTurnToPlay = true;
        v = Integer.MIN_VALUE;
        System.err.println("Searching BEST MOVE");

        for (GameState newGameState : nextStates) {

            if (deadline.timeUntil() < 50) {
                System.err.println("No more time exit");
                break;
            }

            int alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);
            //System.err.println("look for move " + newGameState.getMove() + " : " + alphaBetaVal);
            if (alphaBetaVal > v) {
                System.err.println("New best move " + newGameState.getMove().toString() + " score  " + alphaBetaVal);
                v = alphaBetaVal;
                bestIndex = index;
            }

            if (v > alpha) {
                alpha = v;
            }

            if (beta <= alpha) {
                break;
            }

            index++;
        }

        return nextStates.get(bestIndex);
    }

    public int gama(GameState gameState, int depth) {
        if (gameState.isOWin()) {
            if (idPlayer == Constants.CELL_O) {
                return Integer.MAX_VALUE - depthInit + depth + 1;
            } else if (idPlayer == Constants.CELL_X) {
                return Integer.MIN_VALUE + depth;
            }
        } else if (gameState.isXWin()) {
            if (idPlayer == Constants.CELL_X) {
                return Integer.MAX_VALUE - depthInit + depth + 1;
            } else if (idPlayer == Constants.CELL_O) {
                return Integer.MIN_VALUE + depth;
            }
        } else {
            int score = 0;
            for (int layer = 0; layer < 4; layer++) {

                for (int row = 0; row < 4; row++) {

                    for (int col = 0; col < 4; col++) {

                        if (layer == 0 || layer == 3) {

                            if (gameState.at(row, col, layer) == idPlayer && (row == 0 || row == 3) && (col == 0 || col == 3)) {
                                score += 7;
                            } else {
                                score += 4;
                            }

                        } else if (gameState.at(row, col, layer) == idPlayer && (row == 1 || row == 2) && (col == 1 || col == 2)) {
                            score += 7;
                        } else {
                            score += 5;
                        }

                    }

                }

            }
            return score;
        }
        return 0;
    }

    public int alphaBeta(GameState gameState, int depth, int alpha, int beta, boolean myTurnToPlay, Deadline deadline) {
        depth--;
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.isEmpty() || gameState.isEOG() || depth == 0 || deadline.timeUntil() < 1000) {
            return gama(gameState, depth);
        }
        int v;

        if (myTurnToPlay) {
            v = Integer.MIN_VALUE;

            // Ordering moves
            HashMap<Integer, ArrayList<GameState>> cache = new HashMap<>();
            for (GameState newGameState : nextStates) {
                int val = gama(newGameState, depth);
                if (cache.get(Integer.MAX_VALUE - val) == null) {
                    cache.put(Integer.MAX_VALUE - val, new ArrayList<GameState>());
                }
                cache.get(Integer.MAX_VALUE - val).add(newGameState);
            }

            Iterator<Integer> it = cache.keySet().iterator();

            while (it.hasNext()) {
                int itValue = it.next();
                for (GameState newGameState : cache.get(itValue)) {
                    int alphaBetaVal;
                    if (valuesAlphaBeta.containsKey(newGameState.hashCode())) {
                        alphaBetaVal = valuesAlphaBeta.get(newGameState.hashCode());
                    } else {
                        alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);
                        valuesAlphaBeta.put(newGameState.hashCode(), alphaBetaVal);
                    }
                    if (alphaBetaVal > v) {
                        v = alphaBetaVal;
                    }

                    if (v > alpha) {
                        alpha = v;
                    }

                    if (beta <= alpha) {
                        break;
                    }

                }
            }
        } else {
            v = Integer.MAX_VALUE - depthInit;

            // Ordering moves
            HashMap<Integer, ArrayList<GameState>> cache = new HashMap<>();
            for (GameState newGameState : nextStates) {
                int val = gama(newGameState, depth);
                if (cache.get(val) == null) {
                    cache.put(val, new ArrayList<GameState>());
                }
                cache.get(val).add(newGameState);
            }
            Iterator<Integer> it = cache.keySet().iterator();
            while (it.hasNext()) {
                int itValue = it.next();
                for (GameState newGameState : cache.get(itValue)) {
                    int alphaBetaVal;
                    if (valuesAlphaBeta.containsKey(newGameState.hashCode())) {
                        alphaBetaVal = valuesAlphaBeta.get(newGameState.hashCode());
                    } else {
                        alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);
                        valuesAlphaBeta.put(newGameState.hashCode(), alphaBetaVal);
                    }
                    if (alphaBetaVal < v) {
                        v = alphaBetaVal;
                    }

                    if (v < beta) {
                        beta = v;
                    }

                    if (beta <= alpha) {
                        break;
                    }

                }
            }

        }
        return v;
    }

}
