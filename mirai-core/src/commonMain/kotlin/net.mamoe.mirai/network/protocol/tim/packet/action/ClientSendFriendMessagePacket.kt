@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.io.core.*
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.internal.toPacket
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ClientPacket
import net.mamoe.mirai.network.protocol.tim.packet.PacketId
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.utils.*


@PacketId(0x00_CDu)
class ClientSendFriendMessagePacket(
        private val botQQ: Long,
        private val targetQQ: Long,
        private val sessionKey: ByteArray,
        private val message: MessageChain
) : ClientPacket() {
    override fun encode(builder: BytePacketBuilder) = with(builder) {
        writeRandom(2)

        writeQQ(botQQ)
        writeHex(TIMProtocol.fixVer2)

        encryptAndWrite(sessionKey) {
            writeQQ(botQQ)
            writeQQ(targetQQ)
            writeHex("00 00 00 08 00 01 00 04 00 00 00 00")
            writeHex("37 0F")//TIM最新: 38 03
            writeQQ(botQQ)
            writeQQ(targetQQ)
            writeFully(md5(buildPacket { writeQQ(targetQQ); writeFully(sessionKey) }.readBytes()))
            writeHex("00 0B")
            writeRandom(2)
            writeTime()
            writeHex("00 00" +
                    "00 00 00 00")

            //消息过多要分包发送
            //如果只有一个
            writeByte(0x01)
            writeByte(0)//第几个包
            writeByte(0)
            //如果大于一个,
            //writeByte(0x02)//数量
            //writeByte(0)//第几个包
            //writeByte(0x91)//why?

            writeHex("00 01 4D 53 47 00 00 00 00 00")
            writeTime()
            writeRandom(4)
            writeHex("00 00 00 00 09 00 86")//TIM最新 0C 00 86
            writeHex(TIMProtocol.messageConst1)//... 85 E9 BB 91
            writeZero(2)

            writePacket(message.toPacket())

            /*
                //Plain text
                val bytes = event.toPacket()
                it.writeByte(0x01)
                it.writeShort(bytes.size + 3)
                it.writeByte(0x01)
                it.writeShort(bytes.size)
                it.write(bytes)*/
        }
    }
}

@PacketId(0x00_CDu)
class ServerSendFriendMessageResponsePacket(input: ByteReadPacket) : ServerPacket(input)