package com.utils.excel.enums;

import com.utils.excel.entity.Position;
import com.utils.excel.entity.Range;
import com.utils.util.Num.RangeInt;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

/**
 * Excel 列名定义
 *
 * @author Jason Xie on 2017/10/15.
 */
public enum Column {
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
    AA, AB, AC, AD, AE, AF, AG, AH, AI, AJ, AK, AL, AM, AN, AO, AP, AQ, AR, AS, AT, AU, AV, AW, AX, AY, AZ,
    BA, BB, BC, BD, BE, BF, BG, BH, BI, BJ, BK, BL, BM, BN, BO, BP, BQ, BR, BS, BT, BU, BV, BW, BX, BY, BZ,
    CA, CB, CC, CD, CE, CF, CG, CH, CI, CJ, CK, CL, CM, CN, CO, CP, CQ, CR, CS, CT, CU, CV, CW, CX, CY, CZ,
    DA, DB, DC, DD, DE, DF, DG, DH, DI, DJ, DK, DL, DM, DN, DO, DP, DQ, DR, DS, DT, DU, DV, DW, DX, DY, DZ,
//    EA,EB,EC,ED,EE,EF,EG,EH,EI,EJ,EK,EL,EM,EN,EO,EP,EQ,ER,ES,ET,EU,EV,EW,EX,EY,EZ,
//    FA,FB,FC,FD,FE,FF,FG,FH,FI,FJ,FK,FL,FM,FN,FO,FP,FQ,FR,FS,FT,FU,FV,FW,FX,FY,FZ,
//    GA,GB,GC,GD,GE,GF,GG,GH,GI,GJ,GK,GL,GM,GN,GO,GP,GQ,GR,GS,GT,GU,GV,GW,GX,GY,GZ,
//    HA,HB,HC,HD,HE,HF,HG,HH,HI,HJ,HK,HL,HM,HN,HO,HP,HQ,HR,HS,HT,HU,HV,HW,HX,HY,HZ,
//    IA,IB,IC,ID,IE,IF,IG,IH,II,IJ,IK,IL,IM,IN,IO,IP,IQ,IR,IS,IT,IU,IV,IW,IX,IY,IZ,
//    JA,JB,JC,JD,JE,JF,JG,JH,JI,JJ,JK,JL,JM,JN,JO,JP,JQ,JR,JS,JT,JU,JV,JW,JX,JY,JZ,
//    KA,KB,KC,KD,KE,KF,KG,KH,KI,KJ,KK,KL,KM,KN,KO,KP,KQ,KR,KS,KT,KU,KV,KW,KX,KY,KZ,
//    LA,LB,LC,LD,LE,LF,LG,LH,LI,LJ,LK,LL,LM,LN,LO,LP,LQ,LR,LS,LT,LU,LV,LW,LX,LY,LZ,
//    MA,MB,MC,MD,ME,MF,MG,MH,MI,MJ,MK,ML,MM,MN,MO,MP,MQ,MR,MS,MT,MU,MV,MW,MX,MY,MZ,
//    NA,NB,NC,ND,NE,NF,NG,NH,NI,NJ,NK,NL,NM,NN,NO,NP,NQ,NR,NS,NT,NU,NV,NW,NX,NY,NZ,
//    OA,OB,OC,OD,OE,OF,OG,OH,OI,OJ,OK,OL,OM,ON,OO,OP,OQ,OR,OS,OT,OU,OV,OW,OX,OY,OZ,
//    PA,PB,PC,PD,PE,PF,PG,PH,PI,PJ,PK,PL,PM,PN,PO,PP,PQ,PR,PS,PT,PU,PV,PW,PX,PY,PZ,
//    QA,QB,QC,QD,QE,QF,QG,QH,QI,QJ,QK,QL,QM,QN,QO,QP,QQ,QR,QS,QT,QU,QV,QW,QX,QY,QZ,
//    RA,RB,RC,RD,RE,RF,RG,RH,RI,RJ,RK,RL,RM,RN,RO,RP,RQ,RR,RS,RT,RU,RV,RW,RX,RY,RZ,
//    SA,SB,SC,SD,SE,SF,SG,SH,SI,SJ,SK,SL,SM,SN,SO,SP,SQ,SR,SS,ST,SU,SV,SW,SX,SY,SZ,
//    TA,TB,TC,TD,TE,TF,TG,TH,TI,TJ,TK,TL,TM,TN,TO,TP,TQ,TR,TS,TT,TU,TV,TW,TX,TY,TZ,
//    UA,UB,UC,UD,UE,UF,UG,UH,UI,UJ,UK,UL,UM,UN,UO,UP,UQ,UR,US,UT,UU,UV,UW,UX,UY,UZ,
//    VA,VB,VC,VD,VE,VF,VG,VH,VI,VJ,VK,VL,VM,VN,VO,VP,VQ,VR,VS,VT,VU,VV,VW,VX,VY,VZ,
//    WA,WB,WC,WD,WE,WF,WG,WH,WI,WJ,WK,WL,WM,WN,WO,WP,WQ,WR,WS,WT,WU,WV,WW,WX,WY,WZ,
//    XA,XB,XC,XD,XE,XF,XG,XH,XI,XJ,XK,XL,XM,XN,XO,XP,XQ,XR,XS,XT,XU,XV,XW,XX,XY,XZ,
//    YA,YB,YC,YD,YE,YF,YG,YH,YI,YJ,YK,YL,YM,YN,YO,YP,YQ,YR,YS,YT,YU,YV,YW,YX,YY,YZ,
//    ZA,ZB,ZC,ZD,ZE,ZF,ZG,ZH,ZI,ZJ,ZK,ZL,ZM,ZN,ZO,ZP,ZQ,ZR,ZS,ZT,ZU,ZV,ZW,ZX,ZY,ZZ,
    ;

    /**
     * 返回坐标位置
     *
     * @param rownum int 行号
     * @return {@link Position}
     */
    public Position position(final int rownum) {
        return new Position(rownum, this.name());
    }

    /**
     * 列名 + 行号
     *
     * @param rownum int 行号
     * @return {@link String}
     */
    public String address(final int rownum) {
        return String.format("%s%d", this.name(), rownum);
    }

    /**
     * A1:A10
     *
     * @param startRownum int 开始行号
     * @param endRownum   int 结束行号
     * @return {@link Range}
     */
    public Range range(final int startRownum, final int endRownum) {
        return Range.of(String.format("%s%d:%s%d", this.name(), startRownum, this.name(), endRownum));
    }

    /**
     * A1:A10
     *
     * @param range {@link RangeInt} 开始和结束行号区间对象
     * @return {@link Range}
     */
    public Range range(final RangeInt range) {
        return Range.of(String.format("%s%d:%s%d", this.name(), range.getMin(), this.name(), range.getMax()));
    }

    /**
     * A1:A10
     *
     * @param startRownum int 开始行号
     * @param endRownum   int 结束行号
     * @return String
     */
    public String rangeString(final int startRownum, final int endRownum) {
        return String.format("%s%d:%s%d", this.name(), startRownum, this.name(), endRownum);
    }

    /**
     * A1:A10
     *
     * @param range {@link RangeInt} 开始和结束行号区间对象
     * @return {@link Range}
     */
    public String rangeString(final RangeInt range) {
        return String.format("%s%d:%s%d", this.name(), range.getMin(), this.name(), range.getMax());
    }

    /**
     * SUM(A1:A10)
     *
     * @param startRownum int 开始行号
     * @param endRownum   int 结束行号
     * @return String
     */
    public String sum(final int startRownum, final int endRownum) {
        return String.format("SUM(%s%d:%s%d)", this.name(), startRownum, this.name(), endRownum);
    }

    /**
     * SUM(A1:A10)
     *
     * @param range {@link RangeInt} 开始和结束行号区间对象
     * @return {@link Range}
     */
    public String sum(final RangeInt range) {
        return String.format("SUM(%s%d:%s%d)", this.name(), range.getMin(), this.name(), range.getMax());
    }

    /**
     * SUM(A1,A2,A10)
     *
     * @param rownums {@link List <Integer:rownum:1>}
     * @return {@link String}
     */
    public String sum(final List<Integer> rownums) {
        Objects.requireNonNull(rownums, "参数【rownums】是必须的");
        return rownums.isEmpty()
                ? ""
                : rownums.stream().map(this::address).collect(joining("+"));
    }

    /**
     * SUM(A1,A2,A10)
     *
     * @param rownums {@link Integer[rownum:1]}
     * @return {@link String}
     */
    public String sum(final Integer... rownums) {
        return sum(Arrays.asList(rownums));
    }

    /**
     * AVG(A1:A10)
     *
     * @param startRownum int 开始行号
     * @param endRownum   int 结束行号
     * @return String
     */
    public String avg(final int startRownum, final int endRownum) {
        return String.format("AVG(%s%d:%s%d)", this.name(), startRownum, this.name(), endRownum);
    }

    /**
     * AVG(A1:A10)
     *
     * @param range {@link RangeInt} 开始和结束行号区间对象
     * @return {@link Range}
     */
    public String avg(final RangeInt range) {
        return String.format("AVG(%s%d:%s%d)", this.name(), range.getMin(), this.name(), range.getMax());
    }

    /**
     * AVG(A1,A2,A10)
     *
     * @param rownums {@link List <Integer:rownum:1>}
     * @return {@link String}
     */
    public String avg(final List<Integer> rownums) {
        Objects.requireNonNull(rownums, "参数【rownums】是必须的");
        return rownums.isEmpty()
                ? ""
                : String.format("(%s)/%d", rownums.stream().map(this::address).collect(joining("+")), rownums.size());
    }

    /**
     * AVG(A1,A2,A10)
     *
     * @param rownums {@link Integer[rownum:1]}
     * @return {@link String}
     */
    public String avg(final Integer... rownums) {
        return avg(Arrays.asList(rownums));
    }

    public static void main(String[] args) {
    }
}
