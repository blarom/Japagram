package com.japagram.resources;

import android.content.Context;

import com.japagram.R;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public final class UtilitiesResourceAccess {
    public static String getLanguageText(@NotNull String language, Context context) {
        switch (language) {
            case Globals.LANG_STR_EN: return context.getResources().getString(R.string.language_label_english).toLowerCase();
            case Globals.LANG_STR_FR: return context.getResources().getString(R.string.language_label_french).toLowerCase();
            case Globals.LANG_STR_ES: return context.getResources().getString(R.string.language_label_spanish).toLowerCase();
            default: return context.getResources().getString(R.string.language_label_english).toLowerCase();
        }
    }
    public static String getString(String requestedString, @NotNull Context context, int resourceMap) {
        switch (resourceMap) {
            case Globals.RESOURCE_MAP_VERB_CONJ_TITLES:
                return context.getString(stringResourceMapVerbConjTitles.get(requestedString));
            case Globals.RESOURCE_MAP_VERB_FAMILIES:
                return context.getString(stringResourceMapVerbFamilies.get(requestedString));
            case Globals.RESOURCE_MAP_TYPES:
                return context.getString(stringResourceMapTypes.get(requestedString));
            default:
                return context.getString(stringResourceMapGeneral.get(requestedString));
        }
    }
    private static final HashMap<String, Integer> stringResourceMapVerbFamilies = createResourceMap(Globals.RESOURCE_MAP_VERB_FAMILIES);
    private static final HashMap<String, Integer> stringResourceMapVerbConjTitles = createResourceMap(Globals.RESOURCE_MAP_VERB_CONJ_TITLES);
    private static final HashMap<String, Integer> stringResourceMapTypes = createResourceMap(Globals.RESOURCE_MAP_TYPES);
    private static final HashMap<String, Integer> stringResourceMapGeneral = createResourceMap(Globals.RESOURCE_MAP_GENERAL);
    private static @NotNull HashMap<String, Integer> createResourceMap(int resourceMap) {
        HashMap<String, Integer> map = new HashMap<>();
        switch (resourceMap) {
            case Globals.RESOURCE_MAP_GENERAL:
                map.put("meanings_in", R.string.meanings_in);
                map.put("unavailable", R.string.unavailable);
                map.put("trans_", R.string.trans_);
                map.put("intrans_", R.string.intrans_);
                map.put("trans_intrans_", R.string.trans_intrans_);
                map.put("verb", R.string.verb);
                map.put("unavailable_select_word_to_see_meanings", R.string.unavailable_select_word_to_see_meanings);
                break;
            case Globals.RESOURCE_MAP_VERB_FAMILIES:
                map.put("verb_family_su", R.string.verb_family_su);
                map.put("verb_family_ku", R.string.verb_family_ku);
                map.put("verb_family_iku", R.string.verb_family_iku);
                map.put("verb_family_yuku", R.string.verb_family_yuku);
                map.put("verb_family_gu", R.string.verb_family_gu);
                map.put("verb_family_bu", R.string.verb_family_bu);
                map.put("verb_family_mu", R.string.verb_family_mu);
                map.put("verb_family_nu", R.string.verb_family_nu);
                map.put("verb_family_rug", R.string.verb_family_rug);
                map.put("verb_family_aru", R.string.verb_family_aru);
                map.put("verb_family_tsu", R.string.verb_family_tsu);
                map.put("verb_family_u", R.string.verb_family_u);
                map.put("verb_family_us", R.string.verb_family_us);
                map.put("verb_family_rui", R.string.verb_family_rui);
                map.put("verb_family_da", R.string.verb_family_da);
                map.put("verb_family_kuru", R.string.verb_family_kuru);
                map.put("verb_family_suru", R.string.verb_family_suru);
                break;
            case Globals.RESOURCE_MAP_VERB_CONJ_TITLES:
                map.put("verb_TitleBasics", R.string.verb_TitleBasics);
                map.put("verb_Basics1", R.string.verb_Basics1);
                map.put("verb_Basics2", R.string.verb_Basics2);
                map.put("verb_Basics3", R.string.verb_Basics3);
                map.put("verb_Basics4", R.string.verb_Basics4);
                map.put("verb_Basics5", R.string.verb_Basics5);
                map.put("verb_Basics6", R.string.verb_Basics6);
                map.put("verb_Basics7", R.string.verb_Basics7);
                map.put("verb_Basics8", R.string.verb_Basics8);
                map.put("verb_Basics9", R.string.verb_Basics9);
                map.put("verb_Basics10", R.string.verb_Basics10);
                map.put("verb_Basics11", R.string.verb_Basics11);
                map.put("verb_Basics12", R.string.verb_Basics12);
                map.put("verb_Basics13", R.string.verb_Basics13);
                map.put("verb_Basics14", R.string.verb_Basics14);
                map.put("verb_Basics15", R.string.verb_Basics15);
                map.put("verb_Basics16", R.string.verb_Basics16);

                map.put("verb_TitleSimpleForm", R.string.verb_TitleSimpleForm);
                map.put("verb_TitleProgressive", R.string.verb_TitleProgressive);
                map.put("verb_TitlePoliteness", R.string.verb_TitlePoliteness);
                map.put("verb_TitleRequest", R.string.verb_TitleRequest);
                map.put("verb_TitleImperative", R.string.verb_TitleImperative);
                map.put("verb_TitleDesire", R.string.verb_TitleDesire);
                map.put("verb_TitleProvisional", R.string.verb_TitleProvisional);
                map.put("verb_TitleVolitional", R.string.verb_TitleVolitional);
                map.put("verb_TitleObligation", R.string.verb_TitleObligation);
                map.put("verb_TitlePresumptive", R.string.verb_TitlePresumptive);
                map.put("verb_TitleAlternative", R.string.verb_TitleAlternative);
                map.put("verb_TitleCausativeA", R.string.verb_TitleCausativeA);
                map.put("verb_TitleCausativePv", R.string.verb_TitleCausativePv);
                map.put("verb_TitlePassive", R.string.verb_TitlePassive);
                map.put("verb_TitlePotential", R.string.verb_TitlePotential);
                map.put("verb_TitleContinuative", R.string.verb_TitleContinuative);
                map.put("verb_TitleCompulsion", R.string.verb_TitleCompulsion);
                map.put("verb_TitleGerund", R.string.verb_TitleGerund);

                map.put("verb_PPr", R.string.verb_PPr);
                map.put("verb_PPs", R.string.verb_PPs);
                map.put("verb_PPrN", R.string.verb_PPrN);
                map.put("verb_PPsN", R.string.verb_PPsN);
                map.put("verb_PlPr", R.string.verb_PlPr);
                map.put("verb_PlPs", R.string.verb_PlPs);
                map.put("verb_PlPrN", R.string.verb_PlPrN);
                map.put("verb_PlPsN", R.string.verb_PlPsN);
                map.put("verb_PPrA", R.string.verb_PPrA);
                map.put("verb_ClPr", R.string.verb_ClPr);
                map.put("verb_ClPrA", R.string.verb_ClPrA);
                map.put("verb_ClPrN", R.string.verb_ClPrN);
                map.put("verb_LPl", R.string.verb_LPl);
                map.put("verb_Hn1", R.string.verb_Hn1);
                map.put("verb_Hn2", R.string.verb_Hn2);
                map.put("verb_Hm1", R.string.verb_Hm1);
                map.put("verb_Hm2", R.string.verb_Hm2);
                map.put("verb_Pl1", R.string.verb_Pl1);
                map.put("verb_Pl2", R.string.verb_Pl2);
                map.put("verb_PlN", R.string.verb_PlN);
                map.put("verb_Hn", R.string.verb_Hn);
                map.put("verb_HnN", R.string.verb_HnN);
                map.put("verb_PPrV", R.string.verb_PPrV);
                map.put("verb_PPrL", R.string.verb_PPrL);
                map.put("verb_Pr3rdPO", R.string.verb_Pr3rdPO);
                map.put("verb_Pba", R.string.verb_Pba);
                map.put("verb_ClPba", R.string.verb_ClPba);
                map.put("verb_ClNba", R.string.verb_ClNba);
                map.put("verb_PNba", R.string.verb_PNba);
                map.put("verb_Plba", R.string.verb_Plba);
                map.put("verb_PlNba", R.string.verb_PlNba);
                map.put("verb_Ptara", R.string.verb_Ptara);
                map.put("verb_PNtara", R.string.verb_PNtara);
                map.put("verb_Pltara", R.string.verb_Pltara);
                map.put("verb_PlNtara", R.string.verb_PlNtara);
                map.put("verb_IIWTS", R.string.verb_IIWTS);
                map.put("verb_P", R.string.verb_P);
                map.put("verb_Ps", R.string.verb_Ps);
                map.put("verb_PN", R.string.verb_PN);
                map.put("verb_PPg", R.string.verb_PPg);
                map.put("verb_PlPg", R.string.verb_PlPg);
                map.put("verb_Pl", R.string.verb_Pl);
                map.put("verb_A", R.string.verb_A);
                map.put("verb_N", R.string.verb_N);
                map.put("verb_APg", R.string.verb_APg);
                map.put("verb_NPg", R.string.verb_NPg);
                map.put("verb_PvPg", R.string.verb_PvPg);
                map.put("verb_PvCond", R.string.verb_PvCond);
                map.put("verb_PsPg", R.string.verb_PsPg);
                map.put("verb_teform", R.string.verb_teform);
                map.put("verb_PPr1", R.string.verb_PPr1);
                map.put("verb_PPr2", R.string.verb_PPr2);
                map.put("verb_PPrN1", R.string.verb_PPrN1);
                map.put("verb_PPrN2", R.string.verb_PPrN2);
                map.put("verb_PPrN3", R.string.verb_PPrN3);
                map.put("verb_PlPr1", R.string.verb_PlPr1);
                map.put("verb_PlPr2", R.string.verb_PlPr2);
                map.put("verb_PlPrN1", R.string.verb_PlPrN1);
                map.put("verb_PlPrN2", R.string.verb_PlPrN2);
                map.put("verb_PlPrN3", R.string.verb_PlPrN3);

                map.put("verb_TitleteformCmp", R.string.verb_TitleteformCmp);
                map.put("verb_teformCmp1", R.string.verb_teformCmp1);
                map.put("verb_teformCmp2", R.string.verb_teformCmp2);
                map.put("verb_teformCmp3", R.string.verb_teformCmp3);
                map.put("verb_teformCmp4", R.string.verb_teformCmp4);
                map.put("verb_teformCmp5", R.string.verb_teformCmp5);
                map.put("verb_teformCmp6", R.string.verb_teformCmp6);
                map.put("verb_teformCmp7", R.string.verb_teformCmp7);
                map.put("verb_teformCmp8", R.string.verb_teformCmp8);
                map.put("verb_teformCmp9", R.string.verb_teformCmp9);
                map.put("verb_teformCmp10", R.string.verb_teformCmp10);
                map.put("verb_teformCmp11", R.string.verb_teformCmp11);
                map.put("verb_teformCmp12", R.string.verb_teformCmp12);
                map.put("verb_teformCmp13", R.string.verb_teformCmp13);
                map.put("verb_teformCmp14", R.string.verb_teformCmp14);

                map.put("verb_TitleteformRequestForPermission", R.string.verb_TitleteformRequestForPermission);
                map.put("verb_teformReq1", R.string.verb_teformReq1);
                map.put("verb_teformReq2", R.string.verb_teformReq2);
                map.put("verb_teformReq3", R.string.verb_teformReq3);
                map.put("verb_teformReq4", R.string.verb_teformReq4);

                map.put("verb_TitleteformConj", R.string.verb_TitleteformConj);
                map.put("verb_teformConj1", R.string.verb_teformConj1);
                map.put("verb_teformConj2", R.string.verb_teformConj2);
                map.put("verb_teformConj3", R.string.verb_teformConj3);
                map.put("verb_teformConj4", R.string.verb_teformConj4);

                map.put("verb_TitleSFConj", R.string.verb_TitleSFConj);
                map.put("verb_SFConj1", R.string.verb_SFConj1);
                map.put("verb_SFConj2", R.string.verb_SFConj2);
                map.put("verb_SFConj3", R.string.verb_SFConj3);
                map.put("verb_SFConj4", R.string.verb_SFConj4);

                map.put("verb_TitlemasuCmp", R.string.verb_TitlemasuCmp);
                map.put("verb_masuCmp1", R.string.verb_masuCmp1);
                map.put("verb_masuCmp2", R.string.verb_masuCmp2);
                map.put("verb_masuCmp3", R.string.verb_masuCmp3);
                map.put("verb_masuCmp4", R.string.verb_masuCmp4);
                map.put("verb_masuCmp5", R.string.verb_masuCmp5);
                map.put("verb_masuCmp6", R.string.verb_masuCmp6);
                map.put("verb_masuCmp7", R.string.verb_masuCmp7);
                map.put("verb_masuCmp8", R.string.verb_masuCmp8);
                map.put("verb_masuCmp9", R.string.verb_masuCmp9);
                map.put("verb_masuCmp10", R.string.verb_masuCmp10);
                map.put("verb_masuCmp11", R.string.verb_masuCmp11);
                map.put("verb_masuCmp12", R.string.verb_masuCmp12);
                map.put("verb_masuCmp13", R.string.verb_masuCmp13);

                map.put("verb_TitleStemMisc", R.string.verb_TitleStemMisc);
                map.put("verb_StemMisc1", R.string.verb_StemMisc1);
                map.put("verb_StemMisc2", R.string.verb_StemMisc2);
                map.put("verb_StemMisc3", R.string.verb_StemMisc3);
                map.put("verb_StemMisc4", R.string.verb_StemMisc4);
                map.put("verb_StemMisc5", R.string.verb_StemMisc5);
                map.put("verb_StemMisc6", R.string.verb_StemMisc6);
                map.put("verb_StemMisc7", R.string.verb_StemMisc7);
                map.put("verb_StemMisc8", R.string.verb_StemMisc8);
                map.put("verb_StemMisc9", R.string.verb_StemMisc9);

                map.put("verb_TitleArch", R.string.verb_TitleArch);
                map.put("verb_Arch1", R.string.verb_Arch1);
                map.put("verb_Arch2", R.string.verb_Arch2);
                map.put("verb_Arch3", R.string.verb_Arch3);
                map.put("verb_Arch4", R.string.verb_Arch4);
                break;
            case Globals.RESOURCE_MAP_TYPES:
                map.put("legend_A", R.string.legend_A);
                map.put("legend_Abr", R.string.legend_Abr);
                map.put("legend_Abs", R.string.legend_Abs);
                map.put("legend_Ac", R.string.legend_Ac);
                map.put("legend_Af", R.string.legend_Af);
                map.put("legend_Ai", R.string.legend_Ai);
                map.put("legend_Aj", R.string.legend_Aj);
                map.put("legend_An", R.string.legend_An);
                map.put("legend_Ana", R.string.legend_Ana);
                map.put("legend_Ano", R.string.legend_Ano);
                map.put("legend_Apn", R.string.legend_Apn);
                map.put("legend_Ati", R.string.legend_Ati);
                map.put("legend_Ato", R.string.legend_Ato);
                map.put("legend_Atr", R.string.legend_Atr);
                map.put("legend_ar", R.string.legend_ar);
                map.put("legend_Ax", R.string.legend_Ax);
                map.put("legend_B", R.string.legend_B);
                map.put("legend_C", R.string.legend_C);
                map.put("legend_CE", R.string.legend_CE);
                map.put("legend_CO", R.string.legend_CO);
                map.put("legend_Col", R.string.legend_Col);
                map.put("legend_coq", R.string.legend_coq);
                map.put("legend_Cu", R.string.legend_Cu);
                map.put("legend_Dg", R.string.legend_Dg);
                map.put("legend_DM", R.string.legend_DM);
                map.put("legend_Dr", R.string.legend_Dr);
                map.put("legend_DW", R.string.legend_DW);
                map.put("legend_Fa", R.string.legend_Fa);
                map.put("legend_Fl", R.string.legend_Fl);
                map.put("legend_Fy", R.string.legend_Fy);
                map.put("legend_GO", R.string.legend_GO);
                map.put("legend_iAC", R.string.legend_iAC);
                map.put("legend_idp", R.string.legend_idp);
                map.put("legend_IES", R.string.legend_IES);
                map.put("legend_JEP", R.string.legend_JEP);
                map.put("legend_LF", R.string.legend_LF);
                map.put("legend_LFt", R.string.legend_LFt);
                map.put("legend_LHm", R.string.legend_LHm);
                map.put("legend_LHn", R.string.legend_LHn);
                map.put("legend_LMt", R.string.legend_LMt);
                map.put("legend_loc", R.string.legend_loc);
                map.put("legend_M", R.string.legend_M);
                map.put("legend_MAC", R.string.legend_MAC);
                map.put("legend_Md", R.string.legend_Md);
                map.put("legend_Mo", R.string.legend_Mo);
                map.put("legend_MSE", R.string.legend_MSE);
                map.put("legend_N", R.string.legend_N);
                map.put("legend_naAC", R.string.legend_naAC);
                map.put("legend_NAdv", R.string.legend_NAdv);
                map.put("legend_Ne", R.string.legend_Ne);
                map.put("legend_NE", R.string.legend_NE);
                map.put("legend_Nn", R.string.legend_Nn);
                map.put("legend_num", R.string.legend_num);
                map.put("legend_Obs", R.string.legend_Obs);
                map.put("legend_OI", R.string.legend_OI);
                map.put("legend_org", R.string.legend_org);
                map.put("legend_P", R.string.legend_P);
                map.put("legend_PC", R.string.legend_PC);
                map.put("legend_Pe", R.string.legend_Pe);
                map.put("legend_Pl", R.string.legend_Pl);
                map.put("legend_PP", R.string.legend_PP);
                map.put("legend_Px", R.string.legend_Px);
                map.put("legend_SI", R.string.legend_SI);
                map.put("legend_Sl", R.string.legend_Sl);
                map.put("legend_Sp", R.string.legend_Sp);
                map.put("legend_Sx", R.string.legend_Sx);
                map.put("legend_T", R.string.legend_T);
                map.put("legend_UNC", R.string.legend_UNC);
                map.put("legend_V", R.string.legend_V);
                map.put("legend_VaruI", R.string.legend_VaruI);
                map.put("legend_VaruT", R.string.legend_VaruT);
                map.put("legend_VbuI", R.string.legend_VbuI);
                map.put("legend_VbuT", R.string.legend_VbuT);
                map.put("legend_VC", R.string.legend_VC);
                map.put("legend_VdaI", R.string.legend_VdaI);
                map.put("legend_VdaT", R.string.legend_VdaT);
                map.put("legend_VguI", R.string.legend_VguI);
                map.put("legend_VguT", R.string.legend_VguT);
                map.put("legend_VikuI", R.string.legend_VikuI);
                map.put("legend_VikuT", R.string.legend_VikuT);
                map.put("legend_VyukuI", R.string.legend_VyukuI);
                map.put("legend_VyukuT", R.string.legend_VyukuT);
                map.put("legend_VkuI", R.string.legend_VkuI);
                map.put("legend_VkuruI", R.string.legend_VkuruI);
                map.put("legend_VkuruT", R.string.legend_VkuruT);
                map.put("legend_VkuT", R.string.legend_VkuT);
                map.put("legend_VmuI", R.string.legend_VmuI);
                map.put("legend_VmuT", R.string.legend_VmuT);
                map.put("legend_VnuI", R.string.legend_VnuI);
                map.put("legend_VnuT", R.string.legend_VnuT);
                map.put("legend_VrugI", R.string.legend_VrugI);
                map.put("legend_VrugT", R.string.legend_VrugT);
                map.put("legend_VruiI", R.string.legend_VruiI);
                map.put("legend_VruiT", R.string.legend_VruiT);
                map.put("legend_VsuI", R.string.legend_VsuI);
                map.put("legend_VsuruI", R.string.legend_VsuruI);
                map.put("legend_VsuruT", R.string.legend_VsuruT);
                map.put("legend_VsuT", R.string.legend_VsuT);
                map.put("legend_VtsuI", R.string.legend_VtsuI);
                map.put("legend_VtsuT", R.string.legend_VtsuT);
                map.put("legend_VuI", R.string.legend_VuI);
                map.put("legend_vul", R.string.legend_vul);
                map.put("legend_VusI", R.string.legend_VusI);
                map.put("legend_VusT", R.string.legend_VusT);
                map.put("legend_VuT", R.string.legend_VuT);
                map.put("legend_Vx", R.string.legend_Vx);
                map.put("legend_ZAc", R.string.legend_ZAc);
                map.put("legend_ZAn", R.string.legend_ZAn);
                map.put("legend_ZAs", R.string.legend_ZAs);
                map.put("legend_ZB", R.string.legend_ZB);
                map.put("legend_ZBb", R.string.legend_ZBb);
                map.put("legend_ZBi", R.string.legend_ZBi);
                map.put("legend_ZBs", R.string.legend_ZBs);
                map.put("legend_ZBt", R.string.legend_ZBt);
                map.put("legend_ZC", R.string.legend_ZC);
                map.put("legend_ZCL", R.string.legend_ZCL);
                map.put("legend_ZEc", R.string.legend_ZEc);
                map.put("legend_ZEg", R.string.legend_ZEg);
                map.put("legend_ZF", R.string.legend_ZF);
                map.put("legend_ZFn", R.string.legend_ZFn);
                map.put("legend_ZG", R.string.legend_ZG);
                map.put("legend_ZGg", R.string.legend_ZGg);
                map.put("legend_ZH", R.string.legend_ZH);
                map.put("legend_ZI", R.string.legend_ZI);
                map.put("legend_ZL", R.string.legend_ZL);
                map.put("legend_ZLw", R.string.legend_ZLw);
                map.put("legend_ZM", R.string.legend_ZM);
                map.put("legend_ZMc", R.string.legend_ZMc);
                map.put("legend_ZMg", R.string.legend_ZMg);
                map.put("legend_ZMj", R.string.legend_ZMj);
                map.put("legend_ZMl", R.string.legend_ZMl);
                map.put("legend_ZMt", R.string.legend_ZMt);
                map.put("legend_ZP", R.string.legend_ZP);
                map.put("legend_ZPh", R.string.legend_ZPh);
                map.put("legend_ZSg", R.string.legend_ZSg);
                map.put("legend_ZSm", R.string.legend_ZSm);
                map.put("legend_ZSp", R.string.legend_ZSp);
                map.put("legend_ZSt", R.string.legend_ZSt);
                map.put("legend_ZZ", R.string.legend_ZZ);

                map.put("legend_NmSu", R.string.legend_NmSu);
                map.put("legend_NmPl", R.string.legend_NmPl);
                map.put("legend_NmU", R.string.legend_NmU);
                map.put("legend_NmC", R.string.legend_NmC);
                map.put("legend_NmPr", R.string.legend_NmPr);
                map.put("legend_NmW", R.string.legend_NmW);
                map.put("legend_NmM", R.string.legend_NmM);
                map.put("legend_NmF", R.string.legend_NmF);
                map.put("legend_NmPe", R.string.legend_NmPe);
                map.put("legend_NmG", R.string.legend_NmG);
                map.put("legend_NmSt", R.string.legend_NmSt);
                map.put("legend_NmO", R.string.legend_NmO);
                map.put("legend_NmI", R.string.legend_NmI);
                break;
            default:
                break;
        }
        return map;
    }

}
