
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
        double beta = Integer.MAX_VALUE;
        double alpha = Integer.MIN_VALUE;
        int depth = 3;
        boolean myTurnToPlay = true;
        double v = Integer.MIN_VALUE;
        System.err.println("Searching BEST MOVE");
        
        int size = nextStates.size();
        
        for (GameState newGameState : nextStates) {
            
            if (deadline.timeUntil() < 50) {
                System.err.println("No more time exit");
                break;
            }
            
            double alphaBetaVal = alphaBeta(newGameState, depth, alpha, beta, !myTurnToPlay, deadline);
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
        System.err.println("Valeur  " + gama2(nextStates.get(bestIndex), depth));
        return nextStates.get(bestIndex);
    }
    
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
    
    public double gama2(GameState gameState, int depth) {
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
            double globalRes = 0;
            double[][] row3dValues = {{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}};
            for (int layer = 0; layer < 4; layer++) {
                double rowValue = 1;
                double[] colValues = {1, 1, 1, 1};
                for (int row = 0; row < 4; row++) {
                    rowValue = 1;
                    for (int col = 0; col < 4; col++) {
                        if (gameState.at(row, col, layer) == idPlayer) {
                            rowValue *= 5;
                            colValues[col] *= 5;
                            row3dValues[row][col] *= 5;
                        } else if (gameState.at(row, col, layer) == idOpponent) {
                            rowValue = 0;
                            colValues[col] = 0;
                            row3dValues[row][col] = 0;
                        }
                    }
                    globalRes += rowValue;
                }
                globalRes += colValues[0] + colValues[1] + colValues[2] + colValues[3];
            }
            
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    globalRes += row3dValues[i][j];
                }
            }

            // Then we consider the 28 diagonal rows
            double[] botToTopDiags = {1, 1, 1, 1};
            double[] TopToBotDiags = {1, 1, 1, 1};
            double[][] horizotalDiags = {{1, 1}, {1, 1}, {1, 1}, {1, 1}};
            
            double topLeftToBotRightDiag = 1;
            double topRightToBotLeftDiag = 1;
            double botLeftToTopRightDiag = 1;
            double botRightToTopLeftDiag = 1;
            
            for (int layer = 0; layer < 4; layer++) {
                double lToRDiag = 1;
                double rToLDiag = 1;
                for (int row = 0; row < 4; row++) {
                    
                    for (int col = 0; col < 4; col++) {
                        if (gameState.at(row, col, layer) == idPlayer) {
                            // First Plan Diagonals
                            if (col == row) {
                                lToRDiag *= 5;
                            } else if (row == 0 && col == 3) {
                                rToLDiag *= 5;
                            } else if (row == 1 && col == 2) {
                                rToLDiag *= 5;
                            } else if (row == 2 && col == 1) {
                                rToLDiag *= 5;
                            } else if (row == 3 && col == 0) {
                                rToLDiag *= 5;
                            }

                            //3D plan Diagonals
                            switch (layer) {
                                case 0:
                                    if (row == 0) {
                                        TopToBotDiags[col] *= 5;
                                        
                                        if (col == 0) {
                                            topLeftToBotRightDiag *= 5;
                                        } else if (col == 3) {
                                            topRightToBotLeftDiag *= 5;
                                        }
                                        
                                    } else if (row == 3) {
                                        botToTopDiags[col] *= 5;
                                        
                                        if (col == 0) {
                                            botLeftToTopRightDiag *= 5;
                                        } else if (col == 3) {
                                            botRightToTopLeftDiag *= 5;
                                        }
                                    }
                                    
                                    if (col == 0) {
                                        horizotalDiags[row][0] *= 5;
                                    } else if (col == 3) {
                                        horizotalDiags[row][1] *= 5;
                                    }
                                    
                                    break;
                                case 1:
                                    if (row == 1) {
                                        TopToBotDiags[col] *= 5;
                                        
                                        if (col == 1) {
                                            topLeftToBotRightDiag *= 5;
                                        } else if (col == 2) {
                                            topRightToBotLeftDiag *= 5;
                                        }
                                        
                                    } else if (row == 2) {
                                        botToTopDiags[col] *= 5;
                                        
                                        if (col == 1) {
                                            botLeftToTopRightDiag *= 5;
                                        } else if (col == 2) {
                                            botRightToTopLeftDiag *= 5;
                                        }
                                    }
                                    
                                    if (col == 1) {
                                        horizotalDiags[row][0] *= 5;
                                    } else if (col == 2) {
                                        horizotalDiags[row][1] *= 5;
                                    }
                                    break;
                                case 2:
                                    if (row == 1) {
                                        botToTopDiags[col] *= 5;
                                        
                                        if (col == 1) {
                                            botRightToTopLeftDiag *= 5;
                                        } else if (col == 2) {
                                            botLeftToTopRightDiag *= 5;
                                        }
                                    } else if (row == 2) {
                                        TopToBotDiags[col] *= 5;
                                        if (col == 1) {
                                            topRightToBotLeftDiag *= 5;
                                        } else if (col == 2) {
                                            topLeftToBotRightDiag *= 5;
                                        }
                                    }
                                    
                                    if (col == 1) {
                                        horizotalDiags[row][1] *= 5;
                                    } else if (col == 2) {
                                        horizotalDiags[row][0] *= 5;
                                    }
                                    break;
                                case 3:
                                    if (row == 0) {
                                        botToTopDiags[col] *= 5;
                                        
                                        if (col == 0) {
                                            botRightToTopLeftDiag *= 5;
                                        } else if (col == 3) {
                                            botLeftToTopRightDiag *= 5;
                                        }
                                        
                                    } else if (row == 3) {
                                        TopToBotDiags[col] *= 5;
                                        
                                        if (col == 0) {
                                            topRightToBotLeftDiag *= 5;
                                        } else if (col == 3) {
                                            topLeftToBotRightDiag *= 5;
                                        }
                                    }
                                    
                                    if (col == 0) {
                                        horizotalDiags[row][1] *= 5;
                                    } else if (col == 3) {
                                        horizotalDiags[row][0] *= 5;
                                    }
                                    break;
                            }
                            
                        } else if (gameState.at(row, col, layer) == idOpponent) {
                            
                            if (col == row) {
                                lToRDiag = 0;
                            } else if (row == 0 && col == 3) {
                                rToLDiag = 0;
                            } else if (row == 1 && col == 2) {
                                rToLDiag = 0;
                            } else if (row == 2 && col == 1) {
                                rToLDiag = 0;
                            } else if (row == 3 && col == 0) {
                                rToLDiag = 0;
                            }

                            //3D plan Diagonals
                            switch (layer) {
                                case 0:
                                    if (row == 0) {
                                        TopToBotDiags[col] = 0;
                                        
                                        if (col == 0) {
                                            topLeftToBotRightDiag = 0;
                                        } else if (col == 3) {
                                            topRightToBotLeftDiag = 0;
                                        }
                                        
                                    } else if (row == 3) {
                                        botToTopDiags[col] = 0;
                                        
                                        if (col == 0) {
                                            botLeftToTopRightDiag = 0;
                                        } else if (col == 3) {
                                            botRightToTopLeftDiag = 0;
                                        }
                                    }
                                    
                                    if (col == 0) {
                                        horizotalDiags[row][0] = 0;
                                    } else if (col == 3) {
                                        horizotalDiags[row][1] = 0;
                                    }
                                    
                                    break;
                                case 1:
                                    if (row == 1) {
                                        TopToBotDiags[col] = 0;
                                        
                                        if (col == 1) {
                                            topLeftToBotRightDiag = 0;
                                        } else if (col == 2) {
                                            topRightToBotLeftDiag = 0;
                                        }
                                        
                                    } else if (row == 2) {
                                        botToTopDiags[col] = 0;
                                        
                                        if (col == 1) {
                                            botLeftToTopRightDiag = 0;
                                        } else if (col == 2) {
                                            botRightToTopLeftDiag = 0;
                                        }
                                    }
                                    
                                    if (col == 1) {
                                        horizotalDiags[row][0] = 0;
                                    } else if (col == 2) {
                                        horizotalDiags[row][1] = 0;
                                    }
                                    break;
                                case 2:
                                    if (row == 1) {
                                        botToTopDiags[col] = 0;
                                        
                                        if (col == 1) {
                                            botRightToTopLeftDiag = 0;
                                        } else if (col == 2) {
                                            botLeftToTopRightDiag = 0;
                                        }
                                    } else if (row == 2) {
                                        TopToBotDiags[col] = 0;
                                        if (col == 1) {
                                            topRightToBotLeftDiag = 0;
                                        } else if (col == 2) {
                                            topLeftToBotRightDiag = 0;
                                        }
                                    }
                                    
                                    if (col == 1) {
                                        horizotalDiags[row][1] = 0;
                                    } else if (col == 2) {
                                        horizotalDiags[row][0] = 0;
                                    }
                                    break;
                                case 3:
                                    if (row == 0) {
                                        botToTopDiags[col] = 0;
                                        
                                        if (col == 0) {
                                            botRightToTopLeftDiag = 0;
                                        } else if (col == 3) {
                                            botLeftToTopRightDiag = 0;
                                        }
                                        
                                    } else if (row == 3) {
                                        TopToBotDiags[col] = 0;
                                        
                                        if (col == 0) {
                                            topRightToBotLeftDiag = 0;
                                        } else if (col == 3) {
                                            topLeftToBotRightDiag = 0;
                                        }
                                    }
                                    
                                    if (col == 0) {
                                        horizotalDiags[row][1] = 0;
                                    } else if (col == 3) {
                                        horizotalDiags[row][0] = 0;
                                    }
                                    break;
                            }
                            
                        }
                    }
                }
                globalRes += lToRDiag + rToLDiag;
            }
            for (int i = 0; i < 4; i++) {
                globalRes += botToTopDiags[i] + TopToBotDiags[i];
            }
            
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 2; j++) {
                    globalRes += horizotalDiags[i][j];
                }
            }
            globalRes += topLeftToBotRightDiag + topRightToBotLeftDiag + botLeftToTopRightDiag + botRightToTopLeftDiag;
            return globalRes;
        }
        return 0;
    }
    
    public double alphaBeta(GameState gameState, int depth, double alpha, double beta, boolean myTurnToPlay, Deadline deadline) {
        depth--;
        Vector<GameState> nextStates = new Vector<GameState>();
        gameState.findPossibleMoves(nextStates);
        
        if (nextStates.isEmpty() || gameState.isEOG() || depth == 0 || deadline.timeUntil() < 1000) {
            return gama2(gameState, depth);
        }
        double v;
        
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
