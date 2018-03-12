/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.programfile;

/**
 * Bytecode instructions of a compiled Ballerina program.
 *
 * @since 0.87
 */
public interface InstructionCodes {

    int NOP = 0;
    int ICONST = 2;
    int FCONST = 3;
    int SCONST = 4;
    int CCONST = 5;
    int BTCONST = 6;
    int ICONST_0 = 7;
    int ICONST_1 = 8;
    int ICONST_2 = 9;
    int ICONST_3 = 10;
    int ICONST_4 = 11;
    int ICONST_5 = 12;
    int FCONST_0 = 13;
    int FCONST_1 = 14;
    int FCONST_2 = 15;
    int FCONST_3 = 16;
    int FCONST_4 = 17;
    int FCONST_5 = 18;
    int BCONST_0 = 19;
    int BCONST_1 = 20;
    int RCONST_NULL = 21;

    int IMOVE = 22;
    int CMOVE = 23;
    int BTMOVE = 24;
    int FMOVE = 25;
    int SMOVE = 26;
    int BMOVE = 27;
    int LMOVE = 28;
    int RMOVE = 29;
    int IALOAD = 29;
    int CALOAD = 30;
    int BTALOAD = 31;
    int FALOAD = 32;
    int SALOAD = 33;
    int BALOAD = 34;
    int LALOAD = 35;
    int RALOAD = 36;
    int JSONALOAD = 37;
    int IGLOAD = 38;
    int CGLOAD = 39;
    int FGLOAD = 40;
    int SGLOAD = 41;
    int BGLOAD = 42;
    int LGLOAD = 43;
    int RGLOAD = 44;

    int IFIELDLOAD = 45;
    int CFIELDLOAD = 46;
    int BTFIELDLOAD = 47;
    int FFIELDLOAD = 48;
    int SFIELDLOAD = 49;
    int BFIELDLOAD = 50;
    int LFIELDLOAD = 51;
    int RFIELDLOAD = 52;

    int MAPLOAD = 53;
    int JSONLOAD = 54;
    int ENUMERATORLOAD = 55;

//    int ISTORE = 53;
//    int CSTORE = 54;
//    int FSTORE = 55;
//    int SSTORE = 56;
//    int BSTORE = 57;
//    int LSTORE = 58;
//    int RSTORE = 59;
    int IASTORE = 56;
    int CASTORE = 57;
    int BTASTORE = 58;
    int FASTORE = 59;
    int SASTORE = 60;
    int BASTORE = 61;
    int LASTORE = 62;
    int RASTORE = 63;
    int JSONASTORE = 64;
    int IGSTORE = 65;
    int CGSTORE = 66;
    int BTGSTORE = 67;
    int FGSTORE = 68;
    int SGSTORE = 69;
    int BGSTORE = 70;
    int LGSTORE = 71;
    int RGSTORE = 72;

    int IFIELDSTORE = 73;
    int CFIELDSTORE = 74;
    int BTFIELDSTORE = 75;
    int FFIELDSTORE = 76;
    int SFIELDSTORE = 77;
    int BFIELDSTORE = 78;
    int LFIELDSTORE = 79;
    int RFIELDSTORE = 80;

    int MAPSTORE = 81;
    int JSONSTORE = 82;

    int IADD = 83;
    int BTADD = 84;
    int FADD = 85;
    int SADD = 86;
    int XMLADD = 87;
    int ISUB = 88;
    int FSUB = 89;
    int IMUL = 90;
    int FMUL = 91;
    int IDIV = 92;
    int FDIV = 93;
    int IMOD = 94;
    int FMOD = 95;
    int INEG = 96;
    int FNEG = 97;
    int BNOT = 98;

    int IEQ = 99;
    int CEQ = 100;
    int BTEQ = 101;
    int FEQ = 102;
    int SEQ = 103;
    int BEQ = 104;
    int REQ = 105;

    int INE = 106;
    int CNE = 107;
    int BTNE = 108;
    int FNE = 109;
    int SNE = 110;
    int BNE = 111;
    int RNE = 112;

    int IGT = 113;
    int FGT = 114;

    int IGE = 115;
    int FGE = 116;

    int ILT = 117;
    int FLT = 118;

    int ILE = 119;
    int FLE = 120;

    int REQ_NULL = 121;
    int RNE_NULL = 122;

    int BR_TRUE = 123;
    int BR_FALSE = 124;

    int GOTO = 125;
    int HALT = 126;
    int TR_RETRY = 127;
    int CALL = 128;
    int VCALL = 129;
    int ACALL = 130;
    int THROW = 131;
    int ERRSTORE = 132;
    int FPCALL = 133;
    int FPLOAD = 134;
    int TCALL = 135;

    int SEQ_NULL = 136;
    int SNE_NULL = 137;

    // Type Conversion related instructions
    int I2C = 138;
    int I2F = 139;
    int I2S = 140;
    int I2B = 141;
    int I2JSON = 142;
    int C2I = 143;
    int C2F = 144;
    int C2S = 145;
    int BT2S = 146;
    int F2I = 147;
    int F2C = 148;
    int F2S = 149;
    int F2B = 150;
    int F2JSON = 151;
    int S2I = 152;
    int S2F = 153;
    int S2B = 154;
    int S2JSON = 155;
    int B2I = 156;
    int B2F = 157;
    int B2S = 158;
    int B2JSON = 159;
    int JSON2I = 160;
    int JSON2F = 161;
    int JSON2S = 162;
    int JSON2B = 163;
    int DT2JSON = 164;
    int DT2XML = 165;
    int T2MAP = 166;
    int T2JSON = 167;
    int MAP2T = 168;
    int JSON2T = 169;
//    int XML2JSON = 170;
//    int JSON2XML = 168;
    int S2XML = 170;
    int XML2S = 171;

    // Type cast
    int I2ANY = 172;
    int C2ANY = 173;
    int BT2ANY = 174;
    int F2ANY = 175;
    int S2ANY = 176;
    int B2ANY = 177;
    int L2ANY = 178;
    int ANY2I = 179;
    int ANY2C = 180;
    int ANY2BT = 181;
    int ANY2F = 182;
    int ANY2S = 183;
    int ANY2B = 184;
    int ANY2L = 185;
    int ANY2JSON = 186;
    int ANY2XML = 187;
    int ANY2MAP = 188;
    int ANY2DT = 189;

    int ANY2E = 190;
    int ANY2T = 191;
    int ANY2CN = 192;
    int CHECKCAST = 193;
    int NULL2JSON = 194;

    int ANY2TYPE = 195;
    int S2JSONX = 196;
    int NULL2S = 197;

    int LOCK = 198;
    int UNLOCK = 199;

    // Transactions
    int TR_BEGIN = 200;
    int TR_END = 201;

    int WRKSEND = 202;
    int WRKRECEIVE = 203;
    int FORKJOIN = 204;
    int WRKSTART = 205;
    int WRKRETURN = 206;

    int INEWARRAY = 207;
    int CNEWARRAY = 208;
    int BTNEWARRAY = 209;
    int FNEWARRAY = 210;
    int SNEWARRAY = 211;
    int BNEWARRAY = 212;
    int LNEWARRAY = 213;
    int RNEWARRAY = 214;
    int JSONNEWARRAY = 215;
    int ARRAYLEN = 216;
    int LENGTHOF = 217;

    int NEWSTRUCT = 218;
    int NEWCONNECTOR = 219;
    int NEWMAP = 220;
    int NEWJSON = 221;
    int NEWTABLE = 222;

    int NEW_INT_RANGE = 223;
    int ITR_NEW = 224;
    int ITR_HAS_NEXT = 225;
    int ITR_NEXT = 226;

    // 227, 228

    int IRET = 229;
    int CRET = 230;
    int BTRET = 231;
    int FRET = 232;
    int SRET = 233;
    int BRET = 234;
    int LRET = 235;
    int RRET = 236;
    int RET = 237;

    int XML2XMLATTRS = 238;
    int XMLATTRS2MAP = 239;
    int XMLATTRLOAD = 240;
    int XMLATTRSTORE = 241;
    int S2QNAME = 242;
    int NEWQNAME = 243;
    int NEWXMLELEMENT = 244;
    int NEWXMLCOMMENT = 245;
    int NEWXMLTEXT = 246;
    int NEWXMLPI = 247;
    int XMLSTORE = 248;
    int XMLLOAD = 249;
    
    int TYPEOF = 250;
    int TYPELOAD = 251;

    int TEQ = 252;
    int TNE = 253;

    int INSTRUCTION_CODE_COUNT = 254;
}