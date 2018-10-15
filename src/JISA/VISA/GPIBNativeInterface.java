package JISA.VISA;

import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.ShortByReference;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * JNA Wrapper for library <b>NI4882</b><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public interface GPIBNativeInterface extends Library {

    public static final int                   T1000s            = (int) 17;
    public static final int                   IbcAUTOPOLL       = (int) 7;
    public static final int                   EADR              = (int) 3;
    public static final int                   IbaHSCableLength  = (int) 31;
    public static final int                   IbaBNA            = (int) 512;
    public static final int                   SDC               = (int) 4;
    public static final int                   ECIC              = (int) 1;
    public static final short                 ValidNRFD         = (short) 4;
    public static final int                   IbaSerialNumber   = (int) 35;
    public static final int                   IbaREADDR         = (int) 6;
    public static final int                   IbcSC             = (int) 10;
    public static final int                   NLend             = (int) 1;
    public static final short                 BusSRQ            = (short) 8192;
    public static final short                 ValidATN          = (short) 64;
    public static final short                 ValidREN          = (short) 16;
    public static final int                   T100s             = (int) 15;
    public static final int                   PPC               = (int) 5;
    public static final int                   PPE               = (int) 96;
    public static final int                   IbcEOT            = (int) 4;
    public static final int                   PPD               = (int) 112;
    public static final int                   IbcTMO            = (int) 3;
    public static final short                 ValidIFC          = (short) 8;
    public static final int                   IbaSAD            = (int) 2;
    public static final int                   EHDL              = (int) 23;
    public static final int                   EDMA              = (int) 8;
    public static final int                   PPU               = (int) 21;
    public static final int                   IbaSRE            = (int) 11;
    public static final int                   T100us            = (int) 3;
    public static final short                 BusDAV            = (short) 256;
    public static final int                   IbaEndBitIsNormal = (int) 26;
    public static final int                   T3ms              = (int) 6;
    public static final int                   ENOL              = (int) 2;
    public static final int                   IbcDMA            = (int) 18;
    public static final int                   IbaSendLLO        = (int) 23;
    public static final int                   NULLend           = (int) 0;
    public static final int                   IbcEOScmp         = (int) 14;
    public static final int                   REM               = (int) (1 << 6);
    public static final int                   ATN               = (int) (1 << 4);
    public static final int                   IbcEOS            = (int) 37;
    public static final int                   IbcLON            = (int) 34;
    public static final int                   LACS              = (int) (1 << 2);
    public static final int                   IbaEOSwrt         = (int) 13;
    public static final int                   SRQI              = (int) (1 << 12);
    public static final int                   IbcHSCableLength  = (int) 31;
    public static final int                   IbaSPollTime      = (int) 24;
    public static final short                 BusNRFD           = (short) 1024;
    public static final int                   CIC               = (int) (1 << 5);
    public static final int                   EFSO              = (int) 12;
    public static final short                 ValidEOI          = (short) 128;
    public static final int                   WCFG              = (int) 24;
    public static final int                   IbaSC             = (int) 10;
    public static final int                   T30us             = (int) 2;
    public static final int                   T300ms            = (int) 10;
    public static final int                   END               = (int) (1 << 13);
    public static final int                   IbaUnAddr         = (int) 27;
    public static final int                   T30s              = (int) 14;
    public static final int                   IbcEOSwrt         = (int) 13;
    public static final int                   IbaRsv            = (int) 33;
    public static final int                   LLO               = (int) 17;
    public static final int                   IbaPP2            = (int) 16;
    public static final int                   UNL               = (int) 63;
    public static final int                   UNT               = (int) 95;
    public static final int                   IbaPPC            = (int) 5;
    public static final int                   IbcEOSchar        = (int) 15;
    public static final int                   IbaTIMING         = (int) 17;
    public static final int                   DTAS              = (int) (1 << 1);
    public static final int                   T1ms              = (int) 5;
    public static final int                   EPWR              = (int) 28;
    public static final int                   BIN               = (int) (1 << 12);
    public static final int                   IbcEOSrd          = (int) 12;
    public static final int                   DABend            = (int) 2;
    public static final int                   IbcUnAddr         = (int) 27;
    public static final int                   DCAS              = (int) (1 << 0);
    public static final int                   IbcSendLLO        = (int) 23;
    public static final int                   T10ms             = (int) 7;
    public static final int                   TIMO              = (int) (1 << 14);
    public static final int                   GTL               = (int) 1;
    public static final int                   IbaIst            = (int) 32;
    public static final int                   T300s             = (int) 16;
    public static final int                   IbcPPollTime      = (int) 25;
    public static final int                   IbcSAD            = (int) 2;
    public static final int                   IbaPAD            = (int) 1;
    public static final int                   IbcSRE            = (int) 11;
    public static final short                 BusIFC            = (short) 2048;
    public static final int                   ENEB              = (int) 7;
    public static final int                   LOK               = (int) (1 << 7);
    public static final int                   IbcTIMING         = (int) 17;
    public static final int                   ETAB              = (int) 20;
    public static final int                   GET               = (int) 8;
    public static final int                   IbaEOScmp         = (int) 14;
    public static final short                 BusEOI            = (short) 32768;
    public static final short                 ValidDAV          = (short) 1;
    public static final int                   T300us            = (int) 4;
    public static final int                   IbaEOSrd          = (int) 12;
    public static final int                   ERR               = (int) (1 << 15);
    public static final int                   T10s              = (int) 13;
    public static final int                   TNONE             = (int) 0;
    public static final int                   ESRQ              = (int) 16;
    public static final int                   IbcEndBitIsNormal = (int) 26;
    public static final int                   IbaPPollTime      = (int) 25;
    public static final short                 ValidSRQ          = (short) 32;
    public static final short                 BusREN            = (short) 4096;
    public static final short                 BusATN            = (short) 16384;
    public static final int                   IbaEOSchar        = (int) 15;
    public static final int                   ECAP              = (int) 11;
    public static final int                   ELCK              = (int) 21;
    public static final int                   ESAC              = (int) 5;
    public static final int                   EOIP              = (int) 10;
    public static final int                   REOS              = (int) (1 << 10);
    public static final short                 ValidNDAC         = (short) 2;
    public static final int                   ALL_SAD           = (int) -1;
    public static final int                   EWIP              = (int) 26;
    public static final int                   IbaAUTOPOLL       = (int) 7;
    public static final int                   IbaEOT            = (int) 4;
    public static final int                   IbaEOS            = (int) 37;
    public static final int                   SPD               = (int) 25;
    public static final int                   SPE               = (int) 24;
    public static final int                   IbaTMO            = (int) 3;
    public static final int                   IbcPP2            = (int) 16;
    public static final int                   IbcRsv            = (int) 33;
    public static final int                   IbcPPC            = (int) 5;
    public static final int                   T30ms             = (int) 8;
    public static final int                   T100ms            = (int) 9;
    public static final int                   NO_SAD            = (int) 0;
    public static final int                   DCL               = (int) 20;
    public static final int                   IbaDMA            = (int) 18;
    public static final int                   T1s               = (int) 11;
    public static final int                   XEOS              = (int) (1 << 11);
    public static final int                   TACS              = (int) (1 << 3);
    public static final int                   EDVR              = (int) 0;
    public static final int                   ESTB              = (int) 15;
    public static final short                 BusNDAC           = (short) 512;
    public static final int                   TCT               = (int) 9;
    public static final int                   IbcIst            = (int) 32;
    public static final int                   IbaLON            = (int) 34;
    public static final int                   EABO              = (int) 6;
    public static final int                   IbcPAD            = (int) 1;
    public static final int                   EBUS              = (int) 14;
    public static final int                   STOPend           = (int) 256;
    public static final int                   T10us             = (int) 1;
    public static final int                   ERST              = (int) 27;
    public static final int                   EARG              = (int) 4;
    public static final int                   RQS               = (int) (1 << 11);
    public static final int                   T3s               = (int) 12;
    public static final int                   IbcREADDR         = (int) 6;
    public static final int                   EARM              = (int) 22;
    public static final int                   IbcSPollTime      = (int) 24;
    public static final int                   CMPL              = (int) (1 << 8);
    public static final int                   ECFG              = (int) 24;
    public final        IntByReference        ibsta             = new IntByReference();
    public final        IntByReference        iberr             = new IntByReference();
    public final        IntByReference        ibcnt             = new IntByReference();
    public final        NativeLongByReference ibcntl            = new NativeLongByReference();

    @Deprecated
    int ibfindA(Pointer udname);

    int ibfindA(String udname);

    @Deprecated
    int ibrdfA(int ud, Pointer filename);

    int ibrdfA(int ud, String filename);

    @Deprecated
    int ibwrtfA(int ud, Pointer filename);

    int ibwrtfA(int ud, String filename);

    int ibfindW(WString udname);

    int ibrdfW(int ud, WString filename);

    int ibwrtfW(int ud, WString filename);

    @Deprecated
    int ibask(int ud, int option, IntByReference v);

    int ibask(int ud, int option, IntBuffer v);

    int ibcac(int ud, int v);

    int ibclr(int ud);

    int ibcmd(int ud, Pointer buf, int cnt);

    int ibcmda(int ud, Pointer buf, int cnt);

    int ibconfig(int ud, int option, int v);

    int ibdev(int boardID, int pad, int sad, int tmo, int eot, int eos);

    int ibexpert(int ud, int option, Pointer Input, Pointer Output);

    int ibgts(int ud, int v);

    int iblck(int ud, int v, int LockWaitTime, Pointer Reserved);

    @Deprecated
    int iblines(int ud, ShortByReference result);

    int iblines(int ud, ShortBuffer result);

    @Deprecated
    int ibln(int ud, int pad, int sad, ShortByReference listen);

    int ibln(int ud, int pad, int sad, ShortBuffer listen);

    int ibloc(int ud);

    int ibnotify(int ud, int mask, Callback Callback, Pointer RefData);

    int ibonl(int ud, int v);

    int ibpct(int ud);

    int ibppc(int ud, int v);

    int ibrd(int ud, Pointer buf, int cnt);

    int ibrda(int ud, Pointer buf, int cnt);

    @Deprecated
    int ibrpp(int ud, Pointer ppr);

    int ibrpp(int ud, String ppr);

    @Deprecated
    int ibrsp(int ud, Pointer spr);

    int ibrsp(int ud, String spr);

    int ibsic(int ud);

    int ibstop(int ud);

    int ibtrg(int ud);

    int ibwait(int ud, int mask);

    int ibwrt(int ud, Pointer buf, int cnt);

    int ibwrta(int ud, Pointer buf, int cnt);

    @Deprecated
    void AllSpoll(int boardID, ShortByReference addrlist, ShortByReference results);

    void AllSpoll(int boardID, short addrlist[], ShortBuffer results);

    void DevClear(int boardID, short addr);

    @Deprecated
    void DevClearList(int boardID, ShortByReference addrlist);

    void DevClearList(int boardID, short addrlist[]);

    @Deprecated
    void EnableLocal(int boardID, ShortByReference addrlist);

    void EnableLocal(int boardID, short addrlist[]);

    @Deprecated
    void EnableRemote(int boardID, ShortByReference addrlist);

    void EnableRemote(int boardID, short addrlist[]);

    @Deprecated
    void FindLstn(int boardID, ShortByReference addrlist, ShortByReference results, int limit);

    void FindLstn(int boardID, short addrlist[], ShortBuffer results, int limit);

    @Deprecated
    void FindRQS(int boardID, ShortByReference addrlist, ShortByReference dev_stat);

    void FindRQS(int boardID, short addrlist[], ShortBuffer dev_stat);

    @Deprecated
    void PPoll(int boardID, ShortByReference result);

    void PPoll(int boardID, ShortBuffer result);

    void PPollConfig(int boardID, short addr, int dataLine, int lineSense);

    @Deprecated
    void PPollUnconfig(int boardID, ShortByReference addrlist);

    void PPollUnconfig(int boardID, short addrlist[]);

    void PassControl(int boardID, short addr);

    void RcvRespMsg(int boardID, Pointer buffer, int cnt, int Termination);

    @Deprecated
    void ReadStatusByte(int boardID, short addr, ShortByReference result);

    void ReadStatusByte(int boardID, short addr, ShortBuffer result);

    void Receive(int boardID, short addr, Pointer buffer, int cnt, int Termination);

    void ReceiveSetup(int boardID, short addr);

    @Deprecated
    void ResetSys(int boardID, ShortByReference addrlist);

    void ResetSys(int boardID, short addrlist[]);

    void Send(int boardID, short addr, Pointer databuf, int datacnt, int eotMode);

    void SendCmds(int boardID, Pointer buffer, int cnt);

    void SendDataBytes(int boardID, Pointer buffer, int cnt, int eot_mode);

    void SendIFC(int boardID);

    void SendLLO(int boardID);

    @Deprecated
    void SendList(int boardID, ShortByReference addrlist, Pointer databuf, int datacnt, int eotMode);

    void SendList(int boardID, short addrlist[], Pointer databuf, int datacnt, int eotMode);

    @Deprecated
    void SendSetup(int boardID, ShortByReference addrlist);

    void SendSetup(int boardID, short addrlist[]);

    @Deprecated
    void SetRWLS(int boardID, ShortByReference addrlist);

    void SetRWLS(int boardID, short addrlist[]);

    @Deprecated
    void TestSRQ(int boardID, ShortByReference result);

    void TestSRQ(int boardID, ShortBuffer result);

    @Deprecated
    void TestSys(int boardID, ShortByReference addrlist, ShortByReference results);

    void TestSys(int boardID, short addrlist[], ShortBuffer results);

    void Trigger(int boardID, short addr);

    @Deprecated
    void TriggerList(int boardID, ShortByReference addrlist);

    void TriggerList(int boardID, short addrlist[]);

    @Deprecated
    void WaitSRQ(int boardID, ShortByReference result);

    void WaitSRQ(int boardID, ShortBuffer result);
}
