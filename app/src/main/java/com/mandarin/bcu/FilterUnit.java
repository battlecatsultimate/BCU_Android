package com.mandarin.bcu;

import java.util.ArrayList;
import java.util.Arrays;

class FilterUnit {
    private ArrayList<String> rarity;
    private ArrayList<String> attack;
    private ArrayList<String> target;
    private ArrayList<String> ability;
    private boolean atksimu;
    private boolean atkorand;
    private boolean tgorand;
    private boolean aborand;
    private boolean empty;
    private int unitnumber;

    FilterUnit(ArrayList<String> rarity, ArrayList<String> attack, ArrayList<String> target,
               ArrayList<String> ability, boolean atksimu, boolean atkorand, boolean tgorand,
               boolean aborand, boolean empty, int unitnumber) {
        this.rarity = rarity;
        this.attack = attack;
        this.target = target;
        this.ability = ability;
        this.atksimu = atksimu;
        this.atkorand = atkorand;
        this.tgorand = tgorand;
        this.aborand = aborand;
        this.unitnumber = unitnumber;
        this.empty = empty;

    }

    ArrayList<String> setRarity(String[] unitrarity) {
        ArrayList<String> filterunit = new ArrayList<>();

        if(!rarity.isEmpty()) {
            for(int i =0;i<unitnumber;i++) {
                boolean add = false;
                for(int j=0;j<rarity.size();j++) {
                    if(unitrarity[i].equals(rarity.get(j))) {
                        add= true;
                        break;
                    }
                }
                if(add) {
                    filterunit.add(String.valueOf(i));
                }
            }
        } else {
            for(int i=0;i<unitnumber;i++) {
                filterunit.add(String.valueOf(i));
            }
        }

        return filterunit;
    }

    ArrayList<String> setAttack(String[][][] unitattack, ArrayList<String> filterunit) {
        ArrayList<Boolean> simu = new ArrayList<>();
        ArrayList<Boolean> remover = new ArrayList<>();

        if(!empty) {
            if(atksimu) {
                for(int i = 0; i< filterunit.size(); i++) {
                    boolean add = false;
                    for (int j = 0; j < unitattack[Integer.parseInt(filterunit.get(i))].length; j++) {
                        ArrayList<String> compare = new ArrayList<>(Arrays.asList(unitattack[Integer.parseInt(filterunit.get(i))][j]));

                        if (!compare.contains("12|0")) {
                            add = true;
                            break;
                        }
                    }

                    if (!add) {
                        simu.add(false);
                    } else {
                        simu.add(true);
                    }
                }
            } else {
                for(int i = 0; i< filterunit.size(); i++) {
                    boolean add = false;
                    for (int j = 0; j < unitattack[Integer.parseInt(filterunit.get(i))].length; j++) {
                        ArrayList<String> compare = new ArrayList<>(Arrays.asList(unitattack[Integer.parseInt(filterunit.get(i))][j]));

                        if (!compare.contains("12|1")) {
                            add = true;
                            break;
                        }
                    }

                    if (!add) {
                        simu.add(false);
                    } else {
                        simu.add(true);
                    }
                }
            }
        } else {
            for(int i =0;i<filterunit.size();i++) {
                simu.add(true);
            }
        }



            if(atkorand) {
                for(int i = 0; i< filterunit.size(); i++) {

                    boolean add = false;
                    for(int j = 0; j<unitattack[Integer.parseInt(filterunit.get(i))].length; j++) {
                        ArrayList<String> compare = new ArrayList<>(Arrays.asList(unitattack[Integer.parseInt(filterunit.get(i))][j]));

                        if(attack.size() == 0) {
                            add= true;
                            break;
                        }

                        if(attack.contains("1")) {
                            String [] wait = compare.get(2).split("\\|");

                            if(Integer.parseInt(wait[1]) != 0 ) {
                                add = true;
                                break;
                            }
                        }

                        if(attack.contains("2")) {
                            String [] wait = compare.get(2).split("\\|");

                            if(Integer.parseInt(wait[1]) < 0) {
                                add = true;
                                break;
                            }
                        }

                        if(attack.contains("3")) {
                            if(compare.size() > 3) {
                                String [] wait = compare.get(3).split("\\|");

                                if(Integer.parseInt(wait[1]) != 0) {
                                    add = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }

                    if(!add) {
                        remover.add(false);
                    } else {
                        remover.add(true);
                    }
                }
            } else {
                for(int i = 0; i< filterunit.size(); i++) {
                    boolean broken = false;
                    for(int j = 0; j<unitattack[Integer.parseInt(filterunit.get(i))].length; j++) {
                        int checker = 0;
                        ArrayList<String> compare = new ArrayList<>(Arrays.asList(unitattack[Integer.parseInt(filterunit.get(i))][j]));

                        if(attack.contains("1")) {
                            String [] wait = compare.get(2).split("\\|");

                            if(Integer.parseInt(wait[1]) != 0 ) {
                                checker++;
                            }
                        }

                        if(attack.contains("2")) {
                            String [] wait = compare.get(2).split("\\|");

                            if(Integer.parseInt(wait[1]) < 0) {
                                checker++;
                            }
                        }

                        if(attack.contains("3")) {
                            if(compare.size() > 3) {
                                String [] wait = compare.get(3).split("\\|");

                                if(Integer.parseInt(wait[1]) != 0) {
                                    checker++;
                                }
                            } else {
                                broken = true;
                                break;
                            }
                        }

                        if(Integer.parseInt(filterunit.get(i)) == 355) {
                            System.out.println(checker);
                        }

                        if(checker == attack.size()) {
                            remover.add(true);
                            break;
                        } else if(j == unitattack[Integer.parseInt(filterunit.get(i))].length-1) {
                            remover.add(false);
                        }
                    }
                    if(broken) {
                        remover.add(false);
                    }
                }
            }



        ArrayList<String> finals = new ArrayList<>();

        for(int i=0;i<filterunit.size();i++) {
            if(simu.get(i) && remover.get(i)) {
                finals.add(filterunit.get(i));
            }
        }

        return finals;
    }

    ArrayList<String> setTarget(String[][][] unittarget, ArrayList<String> filterunit) {
        ArrayList<Boolean> remover = new ArrayList<>();

        if(tgorand) {
            for(int i=0;i<filterunit.size();i++) {
                boolean add = false;

                for(int j=0;j<unittarget[Integer.parseInt(filterunit.get(i))].length;j++) {
                    ArrayList<String> compare = new ArrayList<>(Arrays.asList(unittarget[Integer.parseInt(filterunit.get(i))][j]));

                    if(target.isEmpty()) {
                        add = true;
                        break;
                    }

                    for(int k=0;k<target.size();k++) {
                        if(Integer.parseInt(target.get(k)) < compare.size()) {
                            if(Integer.parseInt(compare.get(Integer.parseInt(target.get(k)))) != 0) {
                                add =true;
                                break;
                            }
                        }
                    }

                    if(add) {
                        break;
                    }
                }

                if(!add) {
                    remover.add(false);
                } else {
                    remover.add(true);
                }
            }
        } else {
            for(int i=0;i<filterunit.size();i++) {
                boolean broken = false;
                for(int j=0;j<unittarget[Integer.parseInt(filterunit.get(i))].length;j++) {
                    ArrayList<String> compare = new ArrayList<>(Arrays.asList(unittarget[Integer.parseInt(filterunit.get(i))][j]));
                    int checker = 0;
                    for(int k=0;k<target.size();k++) {
                        if (Integer.parseInt(target.get(k)) < compare.size()) {
                            if (Integer.parseInt(compare.get(Integer.parseInt(target.get(k)))) != 0) {
                                checker++;
                            }
                        } else {
                            broken = true;
                            break;
                        }
                    }

                    if(broken) {
                        break;
                    }

                    if(checker == target.size()) {
                        remover.add(true);
                        break;
                    } else if(j == unittarget[Integer.parseInt(filterunit.get(i))].length-1) {
                        remover.add(false);
                    }
                }

                if(broken) {
                    remover.add(false);
                }
            }
        }

        ArrayList<String> finals = new ArrayList<>();

        for(int i=0;i<filterunit.size();i++) {
            if(remover.get(i)) {
                finals.add(filterunit.get(i));
            }
        }

        return finals;
    }

    ArrayList<String> setAbility(String[][][] unitabil, ArrayList<String> filterunit) {
        ArrayList<Boolean> remover = new ArrayList<>();

        if(aborand) {
            for(int i=0;i<filterunit.size();i++) {
                boolean add = false;

                for(int j=0;j<unitabil[Integer.parseInt(filterunit.get(i))].length;j++) {
                    ArrayList<String> compare = new ArrayList<>(Arrays.asList(unitabil[Integer.parseInt(filterunit.get(i))][j]));

                    if(ability.isEmpty()) {
                        add = true;
                        break;
                    }

                    for(int k=0;k<ability.size();k++) {
                        if(Integer.parseInt(ability.get(k)) < compare.size()) {
                            try {
                                if (Integer.parseInt(compare.get(Integer.parseInt(ability.get(k)))) != 0) {
                                    add = true;
                                    break;
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    if(add) {
                        break;
                    }
                }

                if(!add) {
                    remover.add(false);
                } else {
                    remover.add(true);
                }
            }
        } else {
            for(int i=0;i<filterunit.size();i++) {
                boolean broken = false;
                for(int j=0;j<unitabil[Integer.parseInt(filterunit.get(i))].length;j++) {
                    ArrayList<String> compare = new ArrayList<>(Arrays.asList(unitabil[Integer.parseInt(filterunit.get(i))][j]));
                    int checker = 0;
                    for(int k=0;k<ability.size();k++) {
                        try {
                            if (Integer.parseInt(ability.get(k)) < compare.size()) {
                                if (Integer.parseInt(compare.get(Integer.parseInt(ability.get(k)))) != 0) {
                                    checker++;
                                }
                            } else {
                                broken = true;
                                break;
                            }
                        } catch (Exception e) {
                            broken = true;
                            break;
                        }

                    }

                    if(broken) {
                        break;
                    }

                    if(checker == ability.size()) {
                        remover.add(true);
                        break;
                    } else if(j == unitabil[Integer.parseInt(filterunit.get(i))].length-1) {
                        remover.add(false);
                    }
                }

                if(broken) {
                    remover.add(false);
                }
            }
        }

        ArrayList<String> finals = new ArrayList<>();

        for(int i=0;i<filterunit.size();i++) {
            if(remover.get(i)) {
                finals.add(filterunit.get(i));
            }
        }

        return finals;
    }
}
