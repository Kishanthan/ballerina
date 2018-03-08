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
    int ICONST_0 = 5;
    int ICONST_1 = 6;
    int ICONST_2 = 7;
    int ICONST_3 = 8;
    int ICONST_4 = 9;
    int ICONST_5 = 10;
    int FCONST_0 = 11;
    int FCONST_1 = 12;
    int FCONST_2 = 13;
    int FCONST_3 = 14;
    int FCONST_4 = 15;
    int FCONST_5 = 16;
    int BCONST_0 = 17;
    int BCONST_1 = 18;
    int RCONST_NULL = 19;

    int IMOVE = 21;
    int CMOVE = 22;
    int FMOVE = 23;
    int SMOVE = 24;
    int BMOVE = 25;
    int LMOVE = 26;
    int RMOVE = 27;
    int IALOAD = 28;
    int CALOAD = 29;
    int FALOAD = 30;
    int SALOAD = 31;
    int BALOAD = 32;
    int LALOAD = 33;
    int RALOAD = 34;
    int JSONALOAD = 35;
    int IGLOAD = 36;
    int CGLOAD = 37;
    int FGLOAD = 38;
    int SGLOAD = 39;
    int BGLOAD = 40;
    int LGLOAD = 41;
    int RGLOAD = 42;

    int IFIELDLOAD = 43;
    int CFIELDLOAD = 44;
    int FFIELDLOAD = 45;
    int SFIELDLOAD = 46;
    int BFIELDLOAD = 47;
    int LFIELDLOAD = 48;
    int RFIELDLOAD = 49;

    int MAPLOAD = 50;
    int JSONLOAD = 51;
    int ENUMERATORLOAD = 52;

    int ISTORE = 53;
    int CSTORE = 54;
    int FSTORE = 55;
    int SSTORE = 56;
    int BSTORE = 57;
    int LSTORE = 58;
    int RSTORE = 59;
    int IASTORE = 60;
    int CASTORE = 61;
    int FASTORE = 62;
    int SASTORE = 63;
    int BASTORE = 64;
    int LASTORE = 65;
    int RASTORE = 66;
    int JSONASTORE = 67;
    int IGSTORE = 68;
    int CGSTORE = 69;
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
    int FEQ = 101;
    int SEQ = 102;
    int BEQ = 103;
    int REQ = 104;

    int INE = 105;
    int CNE = 106;
    int FNE = 107;
    int SNE = 108;
    int BNE = 109;
    int RNE = 110;

    int IGT = 111;
    int FGT = 112;

    int IGE = 113;
    int FGE = 114;

    int ILT = 115;
    int FLT = 116;

    int ILE = 117;
    int FLE = 118;

    int REQ_NULL = 119;
    int RNE_NULL = 120;

    int BR_TRUE = 121;
    int BR_FALSE = 122;

    int GOTO = 123;
    int HALT = 124;
    int TR_RETRY = 125;
    int CALL = 126;
    int VCALL = 127;
    int ACALL = 128;
    int THROW = 129;
    int ERRSTORE = 130;
    int FPCALL = 131;
    int FPLOAD = 132;
    int TCALL = 133;

    int SEQ_NULL = 134;
    int SNE_NULL = 135;

    // Type Conversion related instructions
    int I2F = 136;
    int I2S = 137;
    int I2B = 138;
    int I2JSON = 139;
    int F2I = 140;
    int F2S = 141;
    int F2B = 142;
    int F2JSON = 143;
    int S2I = 144;
    int S2F = 145;
    int S2B = 146;
    int S2JSON = 147;
    int B2I = 148;
    int B2F = 149;
    int B2S = 150;
    int B2JSON = 151;
    int JSON2I = 152;
    int JSON2F = 153;
    int JSON2S = 154;
    int JSON2B = 155;
    int DT2JSON = 156;
    int DT2XML = 157;
    int T2MAP = 158;
    int T2JSON = 159;
    int MAP2T = 160;
    int JSON2T = 161;
    int XML2JSON = 162;
    int JSON2XML = 163;
    int S2XML = 164;
    int XML2S = 165;

    // Type cast
    int I2ANY = 166;
    int F2ANY = 167;
    int S2ANY = 168;
    int B2ANY = 169;
    int L2ANY = 170;
    int ANY2I = 171;
    int ANY2F = 172;
    int ANY2S = 173;
    int ANY2B = 174;
    int ANY2L = 175;
    int ANY2JSON = 176;
    int ANY2XML = 177;
    int ANY2MAP = 178;
    int ANY2DT = 179;

    int ANY2E = 180;
    int ANY2T = 181;
    int ANY2CN = 182;
    int CHECKCAST = 183;
    int NULL2JSON = 184;

    int ANY2TYPE = 185;
    int S2JSONX = 186;
    int NULL2S = 187;

    int LOCK = 188;
    int UNLOCK = 189;

    // Transactions
    int TR_BEGIN = 190;
    int TR_END = 191;

    int WRKSEND = 192;
    int WRKRECEIVE = 193;
    int FORKJOIN = 194;
    int WRKSTART = 195;
    int WRKRETURN = 196;

    int INEWARRAY = 200;
    int CNEWARRAY = 201;
    int FNEWARRAY = 202;
    int SNEWARRAY = 203;
    int BNEWARRAY = 204;
    int LNEWARRAY = 205;
    int RNEWARRAY = 206;
    int JSONNEWARRAY = 207;
    int ARRAYLEN = 208;
    int LENGTHOF = 209;

    int NEWSTRUCT = 210;
    int NEWCONNECTOR = 211;
    int NEWMAP = 212;
    int NEWJSON = 213;
    int NEWTABLE = 214;

    int NEW_INT_RANGE = 219;
    int ITR_NEW = 220;
    int ITR_HAS_NEXT = 221;
    int ITR_NEXT = 222;

    int IRET = 230;
    int CRET = 231;
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

    int S2C = 215;
    int F2C = 216;
    int C2F = 217;
    int I2C = 218;
    int CCONST = 223;
    int C2ANY = 224;
    int ANY2C = 225;
    int C2I = 226;
    int C2S = 227;

    int INSTRUCTION_CODE_COUNT = 254;
}
