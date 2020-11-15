package com.mandarin.bcu.androidutil.pack;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mandarin.bcu.androidutil.StaticStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PackConflict {
    public static final List<PackConflict> conflicts = new ArrayList<>();

    public static final int ID_PARENT = 0;
    public static final int ID_CORRUPTED = 1;
    public static final int ID_SAME_ID = 2;
    public static final int ID_UNSUPPORTED_CORE_VERSION = 3;

    public static final int ACTION_NONE = -1;
    public static final int ACTION_IGNORE = 0;
    public static final int ACTION_DELETE = 1;

    public static final int ERR_FILE = 0;
    public static final int ERR_ACTION = 1;
    public static final int ERR_INDEX = 2;

    private final int id;
    private final List<String> confPack;
    private final boolean solvable;
    private int action = ACTION_NONE;
    private boolean solved = false;

    private int err = -1;
    private String msg = "";

    public PackConflict(int id, List<String> confPack, boolean solvable) {
        this.id = id;
        this.confPack = confPack;
        this.solvable = solvable;

        String result = checkIfIDsame();

        if(id == ID_CORRUPTED)
            action = ACTION_DELETE;

        if(result.isEmpty()) {
            conflicts.add(this);
        } else {
            if(id == ID_SAME_ID) {
                String[] info = result.split("\\\\");

                if (info.length != 2) {
                    Log.e("PackConflict", "Invalid index length while initializing PackConflict : " + result);
                    err = ERR_INDEX;
                    msg = "Invalid index length while initializing PackConflict : " + result;
                } else {
                    int index = Integer.parseInt(info[0]);

                    conflicts.get(index).merge(confPack);
                }
            } else {
                conflicts.add(this);
            }
        }
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void solve(Context c) {
        switch (id) {
            case ID_PARENT:
                if(action == ACTION_DELETE) {
                    if(confPack.size() < 2)
                        return;

                    String p = confPack.get(0);
                    String path = StaticStore.getExternalPack(c);

                    File f = new File(path, p);

                    if(f.exists()) {
                        if(!f.delete()) {
                            Log.e("PackConflict", "Failed to remove file "+f.getAbsolutePath());
                            err = ERR_FILE;
                            msg = "Failed to remove file "+f.getAbsolutePath();
                            return;
                        }
                    } else {
                        Log.e("PackConflict", "File not existing, ID : "+confPack.get(0));
                    }

                    solved = true;

                    Log.i("PackConflict", "Delete approved : "+confPack.get(0));
                } else if(action == ACTION_IGNORE) {
                    Log.i("PackConflict", "Ignore approved : "+confPack.get(0));

                    solved = true;
                } else {
                    Log.e("PackConflict", "Invalid action ID in PARENT : "+action);
                    err = ERR_ACTION;
                    msg = "Invalid action ID in PARENT : "+action;
                }

                break;
            case ID_CORRUPTED:
                if(action == ACTION_DELETE) {
                    if(confPack.isEmpty())
                        return;

                    String p = confPack.get(0);

                    String path;

                    if(p.endsWith(".pack.bcuzip")) {
                        path = StaticStore.getExternalPack(c);
                    } else {
                        Log.e("PackConflict", "Invalid File : "+p);

                        return;
                    }

                    File f = new File(path, p);

                    if(f.exists()) {
                        if(!f.delete()) {
                            Log.e("PackConflict", "Failed to remove file "+f.getAbsolutePath());
                            err = ERR_FILE;
                            msg = "Failed to remove file "+f.getAbsolutePath();
                            return;
                        }
                    }

                    solved = true;

                    Log.i("PackConflict", "Delete approved : "+confPack.get(0));
                } else {
                    Log.e("PackConflict", "Invalid action ID in CORRUPTED : "+action);
                    err = ERR_ACTION;
                    msg = "Invalid action ID in CORRUPTED : "+action;
                }

                break;
            case ID_SAME_ID:
                if(action < confPack.size()) {
                    for(int i = 0; i < confPack.size(); i++) {
                        if(i == action) {
                            continue;
                        }

                        String p = confPack.get(i);
                        String path = StaticStore.getExternalPack(c);

                        File f = new File(path, p);

                        if(f.exists()) {
                            if(!f.delete()) {
                                Log.e("PackConflict", "Failed to remove file "+f.getAbsolutePath());
                                err = ERR_FILE;
                                msg = "Failed to remove file "+f.getAbsolutePath();
                            }
                        } else {
                            Log.e("PackConflict","File not existing, ID : "+confPack.get(i));
                        }
                    }
                } else {
                    Log.e("PackConflict", "Invalid action index in SAME_ID : "+action+"\nCONFPACK : "+confPack);
                    err = ERR_INDEX;
                    msg = "Invalid action index in SAME_ID : "+action+"\nCONFPACK : "+confPack;
                }

                break;
            case ID_UNSUPPORTED_CORE_VERSION:
                if(action == ACTION_DELETE) {
                    if(confPack.isEmpty())
                        return;

                    String p = confPack.get(0);

                    String path;

                    if(p.endsWith(".pack.bcuzip")) {
                        path = StaticStore.getExternalPack(c);
                    } else {
                        Log.e("PackConflict", "Invalid File : "+p);

                        return;
                    }

                    File f = new File(path, p);

                    if(f.exists()) {
                        if(!f.delete()) {
                            Log.e("PackConflict", "Failed to remove file "+f.getAbsolutePath());
                            err = ERR_FILE;
                            msg = "Failed to remove file "+f.getAbsolutePath();
                            return;
                        }
                    }

                    solved = true;

                    Log.i("PackConflict", "Delete approved : "+confPack.get(0));
                } else if(action == ACTION_IGNORE) {
                    Log.i("PackConflict", "Ignore approved : "+confPack.get(0));

                    solved = true;
                } else {
                    Log.e("PackConflict", "Invalid action index in SAME_ID : "+action+"\nCONFPACK : "+confPack);
                    err = ERR_INDEX;
                    msg = "Invalid action index in SAME_ID : "+action+"\nCONFPACK : "+confPack;
                }
            default:
                Log.e("PackConflict", "Invalid ID : "+id);
                solved = true;
        }
    }

    public boolean isSolved() {
        return solved;
    }

    public List<String> getConfPack() {
        return confPack;
    }

    public int getId() {
        return id;
    }

    public String checkIfIDsame() {
        for(int i = 0; i < conflicts.size(); i++) {
            List<String> ocp = conflicts.get(i).getConfPack();

            for(String p : confPack) {
                if(ocp.contains(p) && id == ID_SAME_ID) {
                    return ""+i+"\\"+ocp.indexOf(p);
                }
            }
        }

        return "";
    }

    public int getErr() {
        return err;
    }

    public String getMsg() {
        return msg;
    }

    public void merge(List<String> src) {
        src.removeAll(confPack);

        confPack.addAll(src);
    }

    @NonNull
    @Override
    public String toString() {
        if(confPack.size() != 0) {
            return "CONFLICT : "+confPack.get(0)+" | ID : "+id;
        }

        return "";
    }

    public int getAction() {
        return action;
    }

    public boolean isValid(int... position) {
        if(id == ID_SAME_ID) {
            if(position.length == 0)
                return true;

            for(PackConflict pc : conflicts) {
                if(pc.action == ACTION_DELETE && (pc.id == ID_UNSUPPORTED_CORE_VERSION || pc.id == ID_PARENT)) {
                    if(position[0] >= 0 && position[0] < confPack.size()) {
                        String op = confPack.get(position[0]);

                        if(pc.confPack.isEmpty())
                            return true;

                        String cp = pc.getConfPack().get(0);

                        if(op.equals(cp))
                            return false;
                    } else {
                        return true;
                    }
                }
            }

            return true;
        } else if(id == ID_PARENT) {
            if(position.length == 0)
                return true;

            for(PackConflict pc : conflicts) {
                if(pc.action != ACTION_NONE && pc.id == ID_SAME_ID) {
                    if(pc.action >= 0 && pc.action < pc.getConfPack().size()) {
                        if(confPack.isEmpty())
                            return true;

                        String op = confPack.get(0);

                        String cp = pc.getConfPack().get(pc.action);

                        if(!op.equals(cp) || position[0] != 1)
                            return true;
                    } else {
                        return true;
                    }
                } else if(pc.action == ACTION_DELETE && pc.id == ID_UNSUPPORTED_CORE_VERSION) {
                    if(confPack.isEmpty())
                        return true;

                    String op = confPack.get(0);

                    if(pc.confPack.isEmpty())
                        return true;

                    String cp = pc.confPack.get(0);

                    if(!op.equals(cp) || position[0] != 1)
                        return true;
                }
            }

            return true;
        } else if(id == ID_UNSUPPORTED_CORE_VERSION) {
            if(position.length == 0)
                return true;

            for(PackConflict pc : conflicts) {
                if(pc.action == ACTION_DELETE && pc.id == ID_PARENT) {
                    if(confPack.isEmpty())
                        return true;

                    String op = confPack.get(0);

                    if(pc.confPack.isEmpty())
                        return true;

                    String cp = pc.confPack.get(0);

                    if(!op.equals(cp) || position[0] != 1)
                        return true;
                } else if(pc.action != ACTION_NONE && pc.id == ID_SAME_ID) {
                    if(pc.action >= 0 && pc.action < pc.confPack.size()) {
                        if(confPack.isEmpty())
                            return true;

                        String op = confPack.get(0);

                        String cp = pc.confPack.get(pc.action);

                        if(op.equals(cp) || position[0] != 1)
                            return true;
                    }
                }
            }
        }

        return true;
    }

    public boolean isSolvable() {
        return solvable;
    }
}
