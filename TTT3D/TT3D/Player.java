
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
    // Current id of the player
    int idPlayer;
    // We stock the values of alpha beta ( repeated states )
    HashMap<Integer, Double> valuesAlphaBeta;

    public GameState play(final GameState gameState, final Deadline deadline) {
        valuesAlphaBeta = new HashMap<>();
        idPlayer = gameState.getNextPlayer() == 1 ? 2 : 1;
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        /*
        Random random = new Random();
        return nextStates.elementAt(random.nextInt(nextStates.size()));
         */
        int bestIndex = 0;
        int index = 0;
        int depth = 3;
        double beta = Integer.MAX_VALUE;
        double alpha = Integer.MIN_VALUE;
        double v = Integer.MIN_VALUE;
        boolean myTurnToPlay = true;

        System.err.println("Searching BEST MOVE");

        for (GameState newGameState : nextStates) {

            if (deadline.timeUntil() < 50) {
                System.err.println("No more time exit");
                break;
            }

            double alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);

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

    // Old gama function, it doesn't work -> 92 on kattis
    public int gama(GameState gameState, int depth) {
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

    // New gama function we compute our score and the opponent score and we return the soustraction.
    // We consider all the possibilities to win ( 76 ways cf Kattis )
    // We compute like this for a certain number of marks on one line
    // 1 mark  = 1 , 2 marks = 5^2 , 3 marks = 5^3 , 4 marks = + infinity
    public double gama2(GameState gameState) {
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
            int idOpponent = idPlayer == 1 ? 2 : 1;
            // Fist we consider the 48 orthogonal rows
            double globalResMe = 0;
            double globalResOppo = 0;
            double[][] row3dValuesMe = {{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}};
            double[][] row3dValuesOppo = {{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}};
            for (int layer = 0; layer < 4; layer++) {
                double rowValueMe = 1;
                double rowValueOppo = 1;
                double[] colValuesMe = {1, 1, 1, 1};
                double[] colValuesOppo = {1, 1, 1, 1};
                for (int row = 0; row < 4; row++) {
                    rowValueMe = 1;
                    rowValueOppo = 1;
                    for (int col = 0; col < 4; col++) {
                        if (gameState.at(row, col, layer) == idPlayer) {
                            rowValueMe *= 5;
                            colValuesMe[col] *= 5;
                            row3dValuesMe[row][col] *= 5;
                        } else if (gameState.at(row, col, layer) == idOpponent) {
                            rowValueOppo *= 5;
                            colValuesOppo[col] *= 5;
                            row3dValuesOppo[row][col] *= 5;
                        }
                    }
                    globalResMe += rowValueMe;
                    globalResOppo += rowValueOppo;
                }
                globalResMe += colValuesMe[0] + colValuesMe[1] + colValuesMe[2] + colValuesMe[3];
                globalResOppo += colValuesOppo[0] + colValuesOppo[1] + colValuesOppo[2] + colValuesOppo[3];
            }

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    globalResMe += row3dValuesMe[i][j];
                    globalResOppo += row3dValuesOppo[i][j];
                }
            }

            // Then we consider the 28 diagonal rows 
            double[] botToTopDiagsMe = {1, 1, 1, 1};
            double[] TopToBotDiagsMe = {1, 1, 1, 1};
            double[][] horizotalDiagsMe = {{1, 1}, {1, 1}, {1, 1}, {1, 1}};

            double topLeftToBotRightDiagMe = 1;
            double topRightToBotLeftDiagMe = 1;
            double botLeftToTopRightDiagMe = 1;
            double botRightToTopLeftDiagMe = 1;

            double[] botToTopDiagsOppo = {1, 1, 1, 1};
            double[] TopToBotDiagsOppo = {1, 1, 1, 1};
            double[][] horizotalDiagsOppo = {{1, 1}, {1, 1}, {1, 1}, {1, 1}};

            double topLeftToBotRightDiagOppo = 1;
            double topRightToBotLeftDiagOppo = 1;
            double botLeftToTopRightDiagOppo = 1;
            double botRightToTopLeftDiagOppo = 1;

            for (int layer = 0; layer < 4; layer++) {
                double rtoLDiag = 1;
                double ltoRDiag = 1;
                double rtoLDiagOppo = 1;
                double ltoRDiagOppo = 1;
                for (int row = 0; row < 4; row++) {

                    for (int col = 0; col < 4; col++) {
                        if (gameState.at(row, col, layer) == idPlayer) {
                            // First Plan Diagonals
                            if (col == row) {
                                rtoLDiag *= 5;
                            } else if (row == 0 && col == 3) {
                                ltoRDiag *= 5;
                            } else if (row == 1 && col == 2) {
                                ltoRDiag *= 5;
                            } else if (row == 2 && col == 1) {
                                ltoRDiag *= 5;
                            } else if (row == 3 && col == 0) {
                                ltoRDiag *= 5;
                            }

                            //3D plan Diagonals
                            switch (layer) {
                                case 0:
                                    if (row == 0) {
                                        TopToBotDiagsMe[col] *= 5;

                                        if (col == 0) {
                                            topLeftToBotRightDiagMe *= 5;
                                        } else if (col == 3) {
                                            topRightToBotLeftDiagMe *= 5;
                                        }

                                    } else if (row == 3) {
                                        botToTopDiagsMe[col] *= 5;

                                        if (col == 0) {
                                            botLeftToTopRightDiagMe *= 5;
                                        } else if (col == 3) {
                                            botRightToTopLeftDiagMe *= 5;
                                        }
                                    }

                                    if (col == 0) {
                                        horizotalDiagsMe[row][0] *= 5;
                                    } else if (col == 3) {
                                        horizotalDiagsMe[row][1] *= 5;
                                    }

                                    break;
                                case 1:
                                    if (row == 1) {
                                        TopToBotDiagsMe[col] *= 5;

                                        if (col == 1) {
                                            topLeftToBotRightDiagMe *= 5;
                                        } else if (col == 2) {
                                            topRightToBotLeftDiagMe *= 5;
                                        }

                                    } else if (row == 2) {
                                        botToTopDiagsMe[col] *= 5;

                                        if (col == 1) {
                                            botLeftToTopRightDiagMe *= 5;
                                        } else if (col == 2) {
                                            botRightToTopLeftDiagMe *= 5;
                                        }
                                    }

                                    if (col == 1) {
                                        horizotalDiagsMe[row][0] *= 5;
                                    } else if (col == 2) {
                                        horizotalDiagsMe[row][1] *= 5;
                                    }
                                    break;
                                case 2:
                                    if (row == 1) {
                                        botToTopDiagsMe[col] *= 5;

                                        if (col == 1) {
                                            botRightToTopLeftDiagMe *= 5;
                                        } else if (col == 2) {
                                            botLeftToTopRightDiagMe *= 5;
                                        }
                                    } else if (row == 2) {
                                        TopToBotDiagsMe[col] *= 5;
                                        if (col == 1) {
                                            topRightToBotLeftDiagMe *= 5;
                                        } else if (col == 2) {
                                            topLeftToBotRightDiagMe *= 5;
                                        }
                                    }

                                    if (col == 1) {
                                        horizotalDiagsMe[row][1] *= 5;
                                    } else if (col == 2) {
                                        horizotalDiagsMe[row][0] *= 5;
                                    }
                                    break;
                                case 3:
                                    if (row == 0) {
                                        botToTopDiagsMe[col] *= 5;

                                        if (col == 0) {
                                            botRightToTopLeftDiagMe *= 5;
                                        } else if (col == 3) {
                                            botLeftToTopRightDiagMe *= 5;
                                        }

                                    } else if (row == 3) {
                                        TopToBotDiagsMe[col] *= 5;

                                        if (col == 0) {
                                            topRightToBotLeftDiagMe *= 5;
                                        } else if (col == 3) {
                                            topLeftToBotRightDiagMe *= 5;
                                        }
                                    }

                                    if (col == 0) {
                                        horizotalDiagsMe[row][1] *= 5;
                                    } else if (col == 3) {
                                        horizotalDiagsMe[row][0] *= 5;
                                    }
                                    break;
                            }

                        } else if (gameState.at(row, col, layer) == idOpponent) {

                            // First Plan Diagonals for opponent
                            if (col == row) {
                                rtoLDiagOppo *= 5;
                            } else if (row == 0 && col == 3) {
                                ltoRDiagOppo *= 5;
                            } else if (row == 1 && col == 2) {
                                ltoRDiagOppo *= 5;
                            } else if (row == 2 && col == 1) {
                                ltoRDiagOppo *= 5;
                            } else if (row == 3 && col == 0) {
                                ltoRDiagOppo *= 5;
                            }

                            //3D plan Diagonals for opponent
                            switch (layer) {
                                case 0:
                                    if (row == 0) {
                                        TopToBotDiagsOppo[col] *= 5;

                                        if (col == 0) {
                                            topLeftToBotRightDiagOppo *= 5;
                                        } else if (col == 3) {
                                            topRightToBotLeftDiagOppo *= 5;
                                        }

                                    } else if (row == 3) {
                                        botToTopDiagsOppo[col] *= 5;

                                        if (col == 0) {
                                            botLeftToTopRightDiagOppo *= 5;
                                        } else if (col == 3) {
                                            botRightToTopLeftDiagOppo *= 5;
                                        }
                                    }

                                    if (col == 0) {
                                        horizotalDiagsOppo[row][0] *= 5;
                                    } else if (col == 3) {
                                        horizotalDiagsOppo[row][1] *= 5;
                                    }

                                    break;
                                case 1:
                                    if (row == 1) {
                                        TopToBotDiagsOppo[col] *= 5;

                                        if (col == 1) {
                                            topLeftToBotRightDiagOppo *= 5;
                                        } else if (col == 2) {
                                            topRightToBotLeftDiagOppo *= 5;
                                        }

                                    } else if (row == 2) {
                                        botToTopDiagsOppo[col] *= 5;

                                        if (col == 1) {
                                            botLeftToTopRightDiagOppo *= 5;
                                        } else if (col == 2) {
                                            botRightToTopLeftDiagOppo *= 5;
                                        }
                                    }

                                    if (col == 1) {
                                        horizotalDiagsOppo[row][0] *= 5;
                                    } else if (col == 2) {
                                        horizotalDiagsOppo[row][1] *= 5;
                                    }
                                    break;
                                case 2:
                                    if (row == 1) {
                                        botToTopDiagsOppo[col] *= 5;

                                        if (col == 1) {
                                            botRightToTopLeftDiagOppo *= 5;
                                        } else if (col == 2) {
                                            botLeftToTopRightDiagOppo *= 5;
                                        }
                                    } else if (row == 2) {
                                        TopToBotDiagsOppo[col] *= 5;
                                        if (col == 1) {
                                            topRightToBotLeftDiagOppo *= 5;
                                        } else if (col == 2) {
                                            topLeftToBotRightDiagOppo *= 5;
                                        }
                                    }

                                    if (col == 1) {
                                        horizotalDiagsOppo[row][1] *= 5;
                                    } else if (col == 2) {
                                        horizotalDiagsOppo[row][0] *= 5;
                                    }
                                    break;
                                case 3:
                                    if (row == 0) {
                                        botToTopDiagsOppo[col] *= 5;

                                        if (col == 0) {
                                            botRightToTopLeftDiagOppo *= 5;
                                        } else if (col == 3) {
                                            botLeftToTopRightDiagOppo *= 5;
                                        }

                                    } else if (row == 3) {
                                        TopToBotDiagsOppo[col] *= 5;

                                        if (col == 0) {
                                            topRightToBotLeftDiagOppo *= 5;
                                        } else if (col == 3) {
                                            topLeftToBotRightDiagOppo *= 5;
                                        }
                                    }

                                    if (col == 0) {
                                        horizotalDiagsOppo[row][1] *= 5;
                                    } else if (col == 3) {
                                        horizotalDiagsOppo[row][0] *= 5;
                                    }
                                    break;
                            }
                        }
                    }

                }

                globalResMe += rtoLDiag + ltoRDiag;
                globalResOppo += rtoLDiagOppo + ltoRDiagOppo;
            }
            for (int i = 0; i < 4; i++) {
                globalResMe += botToTopDiagsMe[i] + TopToBotDiagsMe[i];
                globalResOppo += botToTopDiagsOppo[i] + TopToBotDiagsOppo[i];
            }

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 2; j++) {
                    globalResMe += horizotalDiagsMe[i][j];
                    globalResOppo += horizotalDiagsOppo[i][j];
                }
            }
            globalResMe += topLeftToBotRightDiagMe + topRightToBotLeftDiagMe + botLeftToTopRightDiagMe + botRightToTopLeftDiagMe;
            globalResOppo += topLeftToBotRightDiagOppo + topRightToBotLeftDiagOppo + botLeftToTopRightDiagOppo + botRightToTopLeftDiagOppo;
            // We return the soustraction
            return globalResMe - globalResOppo;
        }
        return 0;
    }

    public double alphaBeta(GameState gameState, int depth, double alpha, double beta, boolean myTurnToPlay, Deadline deadline) {
        depth--;
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);

        if (nextStates.isEmpty() || gameState.isEOG() || depth == 0 || deadline.timeUntil() < 1000) {
            return gama2(gameState);
        }
        double v;

        if (myTurnToPlay) {
            v = Integer.MIN_VALUE;

            // Ordering moves -> that way we have more chance to prune the tree
            HashMap<Integer, ArrayList<GameState>> cache = new HashMap<>();
            for (GameState newGameState : nextStates) {
                int val = gama(newGameState, depth);
                if (cache.get(Integer.MAX_VALUE - val) == null) {
                    // we use Max_Value - val in order that the maxmimum value of alpha will be the first element of the set list
                    // se we can iterate from maximimum to minimum
                    cache.put(Integer.MAX_VALUE - val, new ArrayList<GameState>());
                }
                cache.get(Integer.MAX_VALUE - val).add(newGameState);
            }

            Iterator<Integer> it = cache.keySet().iterator();

            while (it.hasNext()) {
                int itValue = it.next();
                for (GameState newGameState : cache.get(itValue)) {
                    double alphaBetaVal;
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
            v = Integer.MAX_VALUE;
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
                    double alphaBetaVal;
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
