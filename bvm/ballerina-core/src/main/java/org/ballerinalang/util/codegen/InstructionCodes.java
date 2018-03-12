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
package org.ballerinalang.util.codegen;

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
    int IALOAD = 30;
    int CALOAD = 31;
    int BTALOAD = 32;
    int FALOAD = 33;
    int SALOAD = 34;
    int BALOAD = 35;
    int LALOAD = 36;
    int RALOAD = 37;
    int JSONALOAD = 38;
    int IGLOAD = 39;
    int CGLOAD = 40;
    int BTGLOAD = 41;
    int FGLOAD = 42;
    int SGLOAD = 43;
    int BGLOAD = 44;
    int LGLOAD = 45;
    int RGLOAD = 46;

    int IFIELDLOAD = 47;
    int CFIELDLOAD = 48;
    int FFIELDLOAD = 49;
    int SFIELDLOAD = 50;
    int BFIELDLOAD = 51;
    int LFIELDLOAD = 52;
    int RFIELDLOAD = 53;

    int MAPLOAD = 54;
    int JSONLOAD = 55;
    int ENUMERATORLOAD = 56;

        int ISTORE = 53;
//    int CSTORE = 54;
//    int FSTORE = 55;
//    int SSTORE = 56;
//    int BSTORE = 57;
//    int LSTORE = 58;
//    int RSTORE = 59;
    int IASTORE = 57;
    int CASTORE = 58;
    int BTASTORE = 59;
    int FASTORE = 60;
    int SASTORE = 61;
    int BASTORE = 62;
    int LASTORE = 63;
    int RASTORE = 64;
    int JSONASTORE = 65;
    int IGSTORE = 66;
    int CGSTORE = 67;
    int BTGSTORE = 68;
    int FGSTORE = 70;
    int SGSTORE = 71;
    int BGSTORE = 72;
    int LGSTORE = 73;
    int RGSTORE = 74;

    int IFIELDSTORE = 75;
    int CFIELDSTORE = 76;
    int FFIELDSTORE = 77;
    int SFIELDSTORE = 78;
    int BFIELDSTORE = 79;
    int LFIELDSTORE = 80;
    int RFIELDSTORE = 81;

    int MAPSTORE = 82;
    int JSONSTORE = 83;

    int IADD = 84;
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
    int F2I = 146;
    int F2C = 147;
    int F2S = 148;
    int F2B = 149;
    int F2JSON = 150;
    int S2I = 151;
    int S2F = 152;
    int S2B = 153;
    int S2JSON = 154;
    int B2I = 155;
    int B2F = 156;
    int B2S = 157;
    int B2JSON = 158;
    int JSON2I = 159;
    int JSON2F = 160;
    int JSON2S = 161;
    int JSON2B = 162;
    int DT2JSON = 163;
    int DT2XML = 164;
    int T2MAP = 165;
    int T2JSON = 166;
    int MAP2T = 167;
    int JSON2T = 168;
    //    int XML2JSON = 167;
//    int JSON2XML = 168;
    int S2XML = 169;
    int XML2S = 170;
    int ANY2SCONV = 172;

    // Type cast
    int I2ANY = 173;
    int C2ANY = 174;
    int BT2ANY = 175;
    int F2ANY = 176;
    int S2ANY = 177;
    int B2ANY = 178;
    int L2ANY = 179;
    int ANY2I = 180;
    int ANY2C = 181;
    int ANY2BT = 182;
    int ANY2F = 183;
    int ANY2S = 184;
    int ANY2B = 185;
    int ANY2L = 186;
    int ANY2JSON = 187;
    int ANY2XML = 188;
    int ANY2MAP = 189;
    int ANY2DT = 190;

    int ANY2E = 191;
    int ANY2T = 192;
    int ANY2CN = 193;
    int CHECKCAST = 194;
    int NULL2JSON = 195;

    int ANY2TYPE = 196;
    int S2JSONX = 197;
    int NULL2S = 198;

    int LOCK = 199;
    int UNLOCK = 200;

    // Transactions
    int TR_BEGIN = 201;
    int TR_END = 202;

    int WRKSEND = 203;
    int WRKRECEIVE = 204;
    int FORKJOIN = 205;

    int INEWARRAY = 206;
    int CNEWARRAY = 207;
    int BTNEWARRAY = 208;
    int FNEWARRAY = 209;
    int SNEWARRAY = 210;
    int BNEWARRAY = 211;
    int LNEWARRAY = 212;
    int RNEWARRAY = 213;
    int JSONNEWARRAY = 214;
    int ARRAYLEN = 215;
    int LENGTHOF = 216;

    int NEWSTRUCT = 217;
    int NEWCONNECTOR = 218;
    int NEWMAP = 219;
    int NEWJSON = 220;
    int NEWTABLE = 221;

    int NEW_INT_RANGE = 222;
    int ITR_NEW = 223;
    int ITR_HAS_NEXT = 224;
    int ITR_NEXT = 225;

    //227, 228

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
