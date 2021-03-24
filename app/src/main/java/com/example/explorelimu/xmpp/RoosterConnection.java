package com.example.explorelimu.xmpp;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.explorelimu.R;
import com.example.explorelimu.data.session.Session;
import com.example.explorelimu.data.session.SessionsRepository;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatException;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.packet.MUCUser;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.jid.util.JidUtil;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoosterConnection implements ConnectionListener {

    private static final String TAG = "RoosterConnection";
    private static final String image = "image";
    private static final String file = "file";

    private  final Context mApplicationContext;
    private  final String mDisplayName;
    private  final String mUsername;
    private  final String mPassword;
    private  final String mServiceName = "griffin.chatdiary.com";
    private XMPPTCPConnection mConnection;

    private MessageReceiver chatMessageReceiver;//Receives messages from the ui thread.

    private DomainBareJid xmppServiceDomain;
    private InetAddress localIp;

    private VCardManager vCardManager;

    private boolean justRegistered = false;

    private MultiUserChatManager multiUserChatManager;
    private MultiUserChat multiUserChat;
    private String mucService = "conference.griffin.chatdiary.com";

    private Roster roster;

    private SessionsRepository sessionsRepository;
//    private ChatDatabase chatDatabase;
//    private MsgDao msgDao;
//    private ConversationDao conversationDao;
//    private MessageRepository messageRepository;

    XMPPTCPConnection getmConnection() {
        return mConnection;
    }

    public Roster getRoster() {
        return roster;
    }

    public enum ConnectionState
    {
        CONNECTED ,AUTHENTICATED, CONNECTING ,DISCONNECTING ,DISCONNECTED
    }

    public enum LoggedInState
    {
        LOGGED_IN , LOGGED_OUT
    }


    public RoosterConnection(Context context)
    {
        Log.d(TAG,"RoosterConnection Constructor called.");
        mApplicationContext = context.getApplicationContext();
        mDisplayName = null;
        mUsername = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid",null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password",null);
        sessionsRepository = new SessionsRepository(context).getInstance();
    }

    public RoosterConnection(Context context, String displayName)
    {
        Log.d(TAG,"RoosterConnection Constructor called.");
        mApplicationContext = context.getApplicationContext();
        mDisplayName = displayName;
        mUsername = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid",null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password",null);
    }

    public void initConnection() throws IOException, InterruptedException, XMPPException, SmackException {
        Log.d(TAG, "Connecting to server " + mServiceName);
        xmppServiceDomain = JidCreate.domainBareFrom("griffin.chatdiary.com");
        localIp = InetAddress.getByName(mApplicationContext.getResources().getString(R.string.ip_addr));

        XMPPTCPConnectionConfiguration.Builder builder=
                XMPPTCPConnectionConfiguration.builder();

//        builder.enableDefaultDebugger();
        builder.setHost("griffin.chatdiary.com")
                .setHostAddress(localIp)
                .setXmppDomain(xmppServiceDomain)
                .setUsernameAndPassword(mUsername, mPassword)
                .setResource("Rooster")
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//        builder.setConnectTimeout(50000);

        //Set up the ui thread broadcast message receiver.
        setupUiThreadBroadCastMessageReceivers();

        mConnection = new XMPPTCPConnection(builder.build());

        getmConnection().addConnectionListener(this);

        DeliveryReceiptManager.getInstanceFor(mConnection).autoAddDeliveryReceiptRequests();
        DeliveryReceiptManager.getInstanceFor(mConnection).addReceiptReceivedListener(new ReceiptReceivedListener() {
            @Override
            public void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt) {
                Log.d(TAG, "Receipt received from "+fromJid+" to "+toJid+" with receiptId "+receiptId+" and stanza "+receipt);
                String from = fromJid.toString();

                String contactJid="";
                if ( from.contains("/"))
                {
                    contactJid = from.split("@")[0];
                }else
                {
                    contactJid=from;
                }

//                Intent intent = new Intent(RoosterConnectionService.MESSAGE_UPDATE);
//                intent.setPackage(mApplicationContext.getPackageName());
//                intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID, contactJid);
//                intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_RECEIPT_ID, receiptId);
//                mApplicationContext.sendBroadcast(intent);

                String finalContactJid = contactJid;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        messageRepository.lookupAndUpdateMessage(finalContactJid, receiptId);
                    }
                }).start();
            }
        });

        getmConnection().connect();

        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(getmConnection());
        ReconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

        vCardManager = VCardManager.getInstanceFor(mConnection);

    }

    private void setVCardProperties(String name, String institution){
        try {
            VCard myVCard = vCardManager.loadVCard();
            myVCard.setNickName(name);
            myVCard.setOrganization(institution);
            vCardManager.saveVCard(myVCard);
        } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException e) {
            Log.e(getClass().getName(), e.getMessage());
        }

    }


    public void login() throws IOException, XMPPException, SmackException, InterruptedException {
        initConnection();
        getmConnection().login();

        roster = Roster.getInstanceFor(mConnection);
        getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        getRoster().setRosterLoadedAtLogin(true);

//        Presence presence = new Presence(Presence.Type.available);
//        presence.setStatus("Online, Programmatically!");
//        presence.setPriority(24);
//        presence.setMode(Presence.Mode.available);
//        user.getConnection().sendPacket(presence);

//        Presence presence
        getRoster().addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<Jid> addresses) {
//                for (Jid address : addresses){
//                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                            .setSmallIcon(R.drawable.ic_baseline_person_24)
//                            .setContentTitle(R.string.app_name)
//                            .setContentText(address.toString() + "just subscribed to you")
//                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                }
            }

            @Override
            public void entriesUpdated(Collection<Jid> addresses) {

            }

            @Override
            public void entriesDeleted(Collection<Jid> addresses) {

            }

            @Override
            public void presenceChanged(Presence presence) {
                if (presence.getType() == Presence.Type.subscribe){
                    Presence subscribed = new Presence(Presence.Type.subscribed);
                    subscribed.setTo(presence.getFrom());
                    try {
                        mConnection.sendStanza(subscribed);
                        Log.d(TAG, "Subscription req received from "+presence.getFrom().toString());
                    } catch (SmackException.NotConnectedException | InterruptedException e) {
                        Log.e(TAG, e.toString());
                    }
                } else {
                    String contactJid = presence.getFrom().toString().split("@")[0];
                    Intent intent = new Intent(RoosterConnectionService.PRESENCE_UPDATE);
                    intent.setPackage(mApplicationContext.getPackageName());
                    intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID,contactJid);
                    intent.putExtra(RoosterConnectionService.BUNDLE_PRESENCE_STATUS, presence.isAvailable());
                    mApplicationContext.sendBroadcast(intent);

                    Log.d(TAG,"Received status update from :"+contactJid+" broadcast sent.");
                }
            }
        });

//        if (!roster.isLoaded())
//            roster.reloadAndWait();

//        Collection<RosterEntry> entries = roster.getEntries();
//
//        if (!entries.isEmpty()){
//
//        } else
//            Log.d("Roster", "Roster empty");
//
//        for (RosterEntry entry : entries)
//            Log.d("Roster","Here: " + entry);

        ChatManager.getInstanceFor(getmConnection()).addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid messageFrom, Message message, Chat chat) {
                ///ADDED
                Log.d(TAG,"message.getBody() :"+message.getBody());
                Log.d(TAG,"message.getFrom() :"+message.getFrom());

//                final RmMessage rmMessage;

                String from = message.getFrom().toString();

                String contactJid="";
                if ( from.contains("/"))
                {
                    contactJid = from.split("@")[0];
                    Log.d(TAG,"The real jid is :" + contactJid);
                    Log.d(TAG,"The message is from :" + from);
                }else
                {
                    contactJid=from;
                }

//                if (message.getBody(image) != null) {
//                    rmMessage = new MessageBuilder().vitalFields(contactJid, com.example.chatdiary.chat.Message.Type.IMAGE,
//                            message.getBody(image), new Date()).setMyCaption(message.getBody()).build();
//                } else if (message.getBody("file") != null) {
//                    rmMessage = new MessageBuilder().vitalFields(contactJid, com.example.chatdiary.chat.Message.Type.FILE,
//                            message.getBody(file), new Date()).setMyCaption(message.getBody()).build();
//                } else
//                    rmMessage = new MessageBuilder().vitalFields(contactJid, com.example.chatdiary.chat.Message.Type.TEXT,
//                        message.getBody(), new Date()).build();

                //Bundle up the intent and send the broadcast.
//                Intent intent = new Intent(RoosterConnectionService.NEW_MESSAGE);
//                intent.setPackage(mApplicationContext.getPackageName());
//                intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_OBJ, rmMessage);
//                mApplicationContext.sendBroadcast(intent);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        messageRepository.addFriendMessage(rmMessage);
                    }
                }).start();

                Log.d(TAG,"Received message from :"+contactJid+" broadcast sent.");
                ///ADDED

            }
        });

        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(mConnection);
        multiUserChatManager.addInvitationListener(new InvitationListener() {
            @Override
            public void invitationReceived(XMPPConnection conn, MultiUserChat room, EntityJid inviter, String reason, String password, Message message, MUCUser.Invite invitation) {
                try {
                    room.join(Resourcepart.from(mUsername));
//                    Session newSession = new Session(reason.split("@")[1], reason.split("@")[0], room.getModerators())
                } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException |
                        MultiUserChatException.NotAMucServiceException | XmppStringprepException e) {
                    Log.e(getClass().getName() + " muc error", e.getMessage());
                }

//                Log.d(getClass().getName() + " room", room.getSubject());
//                Session session = new Session(room.getSubject(), inviter)
            }
        });

        multiUserChatManager.setAutoJoinOnReconnect(true);

        if (justRegistered)
            updateName(mDisplayName);
    }

    public void registerUser(String email, String username, String school, Boolean isEducator, String password) throws InterruptedException, XMPPException,
            SmackException, IOException {

        String jid = email.split("@")[0];
        initConnection();
        AccountManager accountManager = AccountManager.getInstance(getmConnection());
        Map<String, String> attributes = new HashMap<>();
        String type = ((isEducator) ? "educator" : "student");

        attributes.put("name", username + "101" + type + "404" + school.replace(" ","909"));
//        attributes.put("text", school);

        attributes.put("email", email);

//        attributes.put("school",)

        if (accountManager.supportsAccountCreation()) {
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(jid),password, attributes);
        }

        justRegistered = true;
    }



    void updateAvatar(byte [] avatarBytes) throws XMPPException.XMPPErrorException, SmackException.NoResponseException, SmackException.NotConnectedException,
            InterruptedException, XmppStringprepException {
        VCard vCard = vCardManager.loadVCard();
//        Uri builtUri = Uri.parse(ExtensionsKt.getDisplayPicPath() + mUsername + ".png");
        vCard.setAvatar(avatarBytes, "b" + avatarBytes);
        vCardManager.saveVCard(vCard);
//        Log.d("avatar url", ExtensionsKt.getDisplayPicPath() + mUsername + ".png");
    }

    void updateName(String mDisplayName){

        VCard vCard = null;
        try {
            vCard = vCardManager.loadVCard();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }

        vCard.setNickName(mDisplayName);
        try {
            vCardManager.saveVCard(vCard);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public byte[] getAvatar() {
        VCard vCard = null;
        try {
            vCard = vCardManager.loadVCard(
                    JidCreate.entityBareFrom(mUsername + "@" + xmppServiceDomain)
            );
        } catch (SmackException.NoResponseException | XmppStringprepException | InterruptedException |
                SmackException.NotConnectedException | XMPPException.XMPPErrorException e) {
            Log.e(TAG, e.getMessage());
        }
        return vCard.getAvatar();
    }

    public void disconnect()
    {
        Log.d(TAG,"Disconnecting from server "+ mServiceName);
        if (getmConnection() != null)
        {
            getmConnection().disconnect();
        }

        mConnection = null;


    }


    @Override
    public void connected(XMPPConnection connection) {
        SASLAuthentication.unBlacklistSASLMechanism("SCRAM-SHA-1");
        SASLAuthentication.unBlacklistSASLMechanism("PLAIN");
        SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");

        RoosterConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"Connected Successfully");
    }


    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        RoosterConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"Authenticated Successfully");
        sendAuthenticatedBroadcast();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);

        if (sharedPreferences.getBoolean("just_registered", false)){
            AccountManager accountManager = AccountManager.getInstance(mConnection);
            try {
                String fullName = accountManager.getAccountAttribute("name");
                setVCardProperties(fullName.substring(0, fullName.indexOf("101")), fullName.substring(1, fullName.indexOf("404")).replace("909", " "));
                sharedPreferences.edit().putBoolean("just_registered", false).apply();
            } catch (SmackException.NoResponseException | XMPPException.XMPPErrorException | SmackException.NotConnectedException | InterruptedException e) {
                Log.e(getClass().getName() + "vCard update", e.getMessage());
            }
        }
    }

    private void sendAuthenticatedBroadcast()
    {
        Intent i = new Intent(RoosterConnectionService.UI_AUTHENTICATED);
//        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(TAG,"Sent the broadcast that we are authenticated");
    }

    @Override
    public void connectionClosed() {
        RoosterConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        Log.d(TAG,"Connectionclosed()");

    }

    @Override
    public void connectionClosedOnError(Exception e) {
        RoosterConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        Log.d(TAG,"ConnectionClosedOnError, error "+ e.toString());

    }

    private void setupUiThreadBroadCastMessageReceivers()
    {
//        chatMessageReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                //Check if the Intents purpose is to send the message.
//                String action = intent.getAction();
//                if( action.equals("com.myapp.main.TEST_INTENT"))
//                {
//                    Log.d(getClass().getName(), "Broadcast from model activity received");
////                    String messageBody = intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY);
////                    int messageId = intent.getIntExtra(RoosterConnectionService.BUNDLE_MESSAGE_ID, -1);
////                    String toJid = intent.getStringExtra(RoosterConnectionService.BUNDLE_TO_JID);
//
////                    RmMessage rmMessage = intent.getParcelableExtra(RoosterConnectionService.BUNDLE_MESSAGE_OBJ);
//
//                    //SENDS THE ACTUAL MESSAGE TO THE SERVER
////                    sendMessage(rmMessage);
//                }
//            }
//        };

        chatMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.SEND_MESSAGE);
        mApplicationContext.registerReceiver(chatMessageReceiver,filter);

//        statusReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                if (action == RoosterConnectionService.SEND_STATUS){
//                    sendMessage(intent.getStringExtra(RoosterConnectionService.STATUS_UPDATE),
//                            intent.getStringExtra(RoosterConnectionService.BUNDLE_TO));
//                }
//            }
//        };

    }

//    private void sendMessage (RmMessage rmMessage)
//    {
//        String toJid = rmMessage.getConversationId().concat("@" + xmppServiceDomain);
//        Log.d(TAG,"Sending message to :"+ toJid);
//
//        EntityBareJid jid = null;
//
//
//        ChatManager chatManager = ChatManager.getInstanceFor(getmConnection());
//
//        try {
//            jid = JidCreate.entityBareFrom(toJid);
//        } catch (XmppStringprepException e) {
//            e.printStackTrace();
//        }
//        Chat chat = chatManager.chatWith(jid);
//        try {
//            Message message = new Message(jid, Message.Type.chat);
//            message.setBody(rmMessage.getContent());
//            String deliveryReceiptId = DeliveryReceiptRequest.addTo(message);
//            rmMessage.setDeliveryReceiptId(deliveryReceiptId);
//            Log.d(TAG, "Message sending with deliveryReceiptId "+deliveryReceiptId);
//            chat.send(message);
//            rmMessage.setStatus(com.example.chatdiary.chat.Message.Status.SENT);
////            Intent intent = new Intent(RoosterConnectionService.MESSAGE_UPDATE);
////            intent.putExtra(
////                    RoosterConnectionService.BUNDLE_MESSAGE_OBJ,
////                    rmMessage
////            );
////            mApplicationContext.sendBroadcast(intent);
//
//            new Thread(() -> messageRepository.updateMessage(rmMessage)).start();
//
//        } catch (SmackException.NotConnectedException | InterruptedException e) {
//            e.printStackTrace();
//            Log.d(TAG,e.toString());
//        }
//    }

    private void initializeMuc(String roomName, String model, List<EntityBareJid> ownersList) throws XmppStringprepException, InterruptedException, SmackException.NoResponseException,
            MultiUserChatException.MucAlreadyJoinedException, SmackException.NotConnectedException, XMPPException.XMPPErrorException,
            MultiUserChatException.MissingMucCreationAcknowledgeException, MultiUserChatException.NotAMucServiceException,
            MultiUserChatException.MucConfigurationNotSupportedException {

        // Create a MultiUserChat using an XMPPConnection for a room
        multiUserChat = multiUserChatManager.getMultiUserChat(JidCreate.entityBareFrom(roomName + mucService));

        // Create the nickname.
        Resourcepart nickname = Resourcepart.from(roomName);

        // Prepare a list of owners of the new room
        Set<Jid> owners = JidUtil.jidSetFrom(ownersList);

        // Create the room
        multiUserChat.create(nickname)
                .getConfigFormManager()
//                .setRoomOwners(owners)
                .submitConfigurationForm();

//        multiUserChat.join(Resourcepart.from(mConnection));
//
        for (EntityBareJid user : ownersList)
            multiUserChat.invite(user, model);

    }

    private void joinMuc(String nickname, EntityBareJid mucJid) throws XmppStringprepException, XMPPException.XMPPErrorException,
            MultiUserChatException.NotAMucServiceException, SmackException.NotConnectedException, InterruptedException, SmackException.NoResponseException {
        // Get the MultiUserChatManager
        MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(mConnection);

        // Create a MultiUserChat using an XMPPConnection for a room
        MultiUserChat muc2 = manager.getMultiUserChat(mucJid);

        // User2 joins the new room
        // The room service will decide the amount of history to send
        muc2.join(Resourcepart.from(nickname));
    }
}
