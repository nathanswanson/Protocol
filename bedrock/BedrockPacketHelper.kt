package protocol.bedrock

import com.nukkitx.math.vector.Vector2f
import java.nio.charset.StandardCharsets
import java.util.function.Function
import java.util.function.Predicate

abstract class BedrockPacketHelper protected constructor() {
    protected val entityData: Int2ObjectBiMap<EntityData> = Int2ObjectBiMap<EntityData>()
    protected val entityFlags: Int2ObjectBiMap<EntityFlag> = Int2ObjectBiMap<EntityFlag>()
    protected val entityDataTypes: Int2ObjectBiMap<EntityData.Type> = Int2ObjectBiMap<EntityData.Type>()
    protected val entityEvents: Int2ObjectBiMap<EntityEventType> = Int2ObjectBiMap<EntityEventType>()
    protected val gameRuleTypes: Object2IntMap<Class<*>> = Object2IntOpenHashMap<Class<*>>(3, 0.5f)
    protected val soundEvents: Int2ObjectBiMap<SoundEvent> = Int2ObjectBiMap<SoundEvent>()
    protected val levelEvents: Int2ObjectBiMap<LevelEventType> = Int2ObjectBiMap<LevelEventType>()
    protected val commandParams: Int2ObjectBiMap<CommandParam> = Int2ObjectBiMap<CommandParam>()
    protected val resourcePackTypes: Int2ObjectBiMap<ResourcePackType> = Int2ObjectBiMap<ResourcePackType>()
    protected fun addGameRuleType(index: Int, clazz: Class<*>?) {
        gameRuleTypes.put(clazz, index)
    }

    protected fun addEntityData(index: Int, entityData: EntityData?) {
        this.entityData.put(index, entityData)
    }

    protected fun addEntityFlag(index: Int, flag: EntityFlag?) {
        entityFlags.put(index, flag)
    }

    protected fun addEntityDataType(index: Int, type: EntityData.Type?) {
        entityDataTypes.put(index, type)
    }

    protected fun addEntityEvent(index: Int, type: EntityEventType?) {
        entityEvents.put(index, type)
    }

    protected fun addSoundEvent(index: Int, soundEvent: SoundEvent?) {
        soundEvents.put(index, soundEvent)
    }

    protected fun addLevelEvent(index: Int, levelEventType: LevelEventType?) {
        levelEvents.put(index, levelEventType)
    }

    fun getEntityEventId(type: EntityEventType?): Int {
        // @TODO For speed we may want a flag that disables this check for production use
        if (!entityEvents.containsValue(type)) {
            log.debug("Unknown EntityEventType: {}", type)
            return entityEvents.get(EntityEventType.NONE)
        }
        return entityEvents.get(type)
    }

    fun getEntityEvent(id: Int): EntityEventType {
        // @TODO For speed we may want a flag that disables this check for production use
        if (!entityEvents.containsKey(id)) {
            log.debug("Unknown EntityEvent: {}", id)
            return EntityEventType.NONE
        }
        return entityEvents.get(id)
    }

    fun getSoundEventId(event: SoundEvent?): Int {
        if (!soundEvents.containsValue(event)) {
            log.debug("Unknown SoundEvent {} received", event)
            return soundEvents.get(SoundEvent.UNDEFINED)
        }
        return soundEvents.get(event)
    }

    fun getSoundEvent(id: Int): SoundEvent {
        val soundEvent: SoundEvent = soundEvents.get(id)
        if (soundEvent == null) {
            log.debug("Unknown SoundEvent {} received", Integer.toUnsignedLong(id))
            return SoundEvent.UNDEFINED
        }
        return soundEvent
    }

    fun getLevelEventId(event: LevelEventType?): Int {
        // @TODO For speed we may want a flag that disables this check for production use
        if (!levelEvents.containsValue(event)) {
            log.debug("Unknown LevelEventType: {}", event)
            return levelEvents.get(LevelEventType.UNDEFINED)
        }
        return levelEvents.get(event)
    }

    fun getLevelEvent(id: Int): LevelEventType {
        val levelEvent: LevelEventType = levelEvents.get(id)
        if (levelEvent == null) {
            log.debug("Unknown LevelEventType {} received", id)
            return LevelEventType.UNDEFINED
        }
        return levelEvent
    }

    fun addCommandParam(index: Int, commandParam: CommandParam?) {
        commandParams.put(index, commandParam)
    }

    fun getCommandParam(index: Int): CommandParam {
        val commandParam: CommandParam = commandParams.get(index)
        if (commandParam == null) {
            log.debug("Requested undefined CommandParam {}", index)
            return CommandParam(index)
        }
        return commandParam
    }

    fun getCommandParamId(commandParam: CommandParam?): Int {
        return commandParams.get(commandParam)
    }

    fun removeCommandParam(index: Int) {
        commandParams.remove(index)
    }

    fun removeCommandParam(type: CommandParam?) {
        commandParams.remove(type)
    }

    fun addResourcePackType(index: Int, resourcePackType: ResourcePackType?) {
        resourcePackTypes.put(index, resourcePackType)
    }

    fun getResourcePackType(index: Int): ResourcePackType {
        return resourcePackTypes.get(index)
    }

    fun getResourcePackTypeId(resourcePackType: ResourcePackType?): Int {
        return resourcePackTypes.get(resourcePackType)
    }

    protected abstract fun registerEntityData()
    protected abstract fun registerEntityFlags()
    protected abstract fun registerEntityDataTypes()
    protected abstract fun registerEntityEvents()
    protected abstract fun registerGameRuleTypes()
    protected abstract fun registerSoundEvents()
    protected abstract fun registerCommandParams()
    protected abstract fun registerResourcePackTypes()
    protected abstract fun registerLevelEvents()
    abstract fun readEntityLink(buffer: ByteBuf?): EntityLinkData?
    abstract fun writeEntityLink(buffer: ByteBuf?, link: EntityLinkData?)
    abstract fun readNetItem(buffer: ByteBuf?, session: BedrockSession?): ItemData?
    abstract fun writeNetItem(buffer: ByteBuf?, item: ItemData?, session: BedrockSession?)
    abstract fun readItem(buffer: ByteBuf?, session: BedrockSession?): ItemData?
    abstract fun writeItem(buffer: ByteBuf?, item: ItemData?, session: BedrockSession?)
    abstract fun readItemInstance(buffer: ByteBuf?, session: BedrockSession?): ItemData?
    abstract fun writeItemInstance(buffer: ByteBuf?, item: ItemData?, session: BedrockSession?)
    abstract fun readCommandOrigin(buffer: ByteBuf?): CommandOriginData?
    abstract fun writeCommandOrigin(buffer: ByteBuf?, commandOrigin: CommandOriginData?)
    abstract fun readGameRule(buffer: ByteBuf?): GameRuleData<*>?
    abstract fun writeGameRule(buffer: ByteBuf?, gameRule: GameRuleData<*>?)
    abstract fun readEntityData(buffer: ByteBuf?, entityData: EntityDataMap?)
    abstract fun writeEntityData(buffer: ByteBuf?, entityData: EntityDataMap?)
    abstract fun readCommandEnum(buffer: ByteBuf?, soft: Boolean): CommandEnumData?
    abstract fun writeCommandEnum(buffer: ByteBuf?, commandEnum: CommandEnumData?)
    abstract fun readStructureSettings(buffer: ByteBuf?): StructureSettings?
    abstract fun writeStructureSettings(buffer: ByteBuf?, settings: StructureSettings?)
    abstract fun readSkin(buffer: ByteBuf?): SerializedSkin?
    abstract fun writeSkin(buffer: ByteBuf?, skin: SerializedSkin?)
    abstract fun readAnimationData(buffer: ByteBuf?): AnimationData?
    abstract fun writeAnimationData(buffer: ByteBuf?, animation: AnimationData?)
    abstract fun readImage(buffer: ByteBuf?): ImageData?
    abstract fun writeImage(buffer: ByteBuf?, image: ImageData?)
    fun readByteArray(buffer: ByteBuf): ByteArray {
        Preconditions.checkNotNull(buffer, "buffer")
        val length: Int = VarInts.readUnsignedInt(buffer)
        Preconditions.checkArgument(
            buffer.isReadable(length),
            "Tried to read %s bytes but only has %s readable", length, buffer.readableBytes()
        )
        val bytes = ByteArray(length)
        buffer.readBytes(bytes)
        return bytes
    }

    fun writeByteArray(buffer: ByteBuf, bytes: ByteArray) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(bytes, "bytes")
        VarInts.writeUnsignedInt(buffer, bytes.size)
        buffer.writeBytes(bytes)
    }

    fun readBuffer(buffer: ByteBuf): ByteBuf {
        val length: Int = VarInts.readUnsignedInt(buffer)
        return buffer.readRetainedSlice(length)
    }

    fun writeBuffer(buffer: ByteBuf, toWrite: ByteBuf) {
        Preconditions.checkNotNull(toWrite, "toWrite")
        VarInts.writeUnsignedInt(buffer, toWrite.readableBytes())
        buffer.writeBytes(toWrite, toWrite.readerIndex(), toWrite.writerIndex())
    }

    fun readString(buffer: ByteBuf): String {
        Preconditions.checkNotNull(buffer, "buffer")
        return String(readByteArray(buffer), StandardCharsets.UTF_8)
    }

    fun writeString(buffer: ByteBuf, string: String) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(string, "string")
        writeByteArray(buffer, string.toByteArray(StandardCharsets.UTF_8))
    }

    fun readLEAsciiString(buffer: ByteBuf): AsciiString {
        Preconditions.checkNotNull(buffer, "buffer")
        val length: Int = buffer.readIntLE()
        val bytes = ByteArray(length)
        buffer.readBytes(bytes)
        return AsciiString(bytes)
    }

    fun writeLEAsciiString(buffer: ByteBuf, string: AsciiString) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(string, "string")
        buffer.writeIntLE(string.length)
        buffer.writeBytes(string.toByteArray())
    }

    fun readUuid(buffer: ByteBuf): UUID {
        Preconditions.checkNotNull(buffer, "buffer")
        return UUID(buffer.readLongLE(), buffer.readLongLE())
    }

    fun writeUuid(buffer: ByteBuf, uuid: UUID) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(uuid, "uuid")
        buffer.writeLongLE(uuid.getMostSignificantBits())
        buffer.writeLongLE(uuid.getLeastSignificantBits())
    }

    fun readVector3f(buffer: ByteBuf): Vector3f {
        Preconditions.checkNotNull(buffer, "buffer")
        val x: Float = buffer.readFloatLE()
        val y: Float = buffer.readFloatLE()
        val z: Float = buffer.readFloatLE()
        return Vector3f.from(x, y, z)
    }

    fun writeVector3f(buffer: ByteBuf, vector3f: Vector3f) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(vector3f, "vector3f")
        buffer.writeFloatLE(vector3f.getX())
        buffer.writeFloatLE(vector3f.getY())
        buffer.writeFloatLE(vector3f.getZ())
    }

    fun readVector2f(buffer: ByteBuf): Vector2f {
        Preconditions.checkNotNull(buffer, "buffer")
        val x: Float = buffer.readFloatLE()
        val y: Float = buffer.readFloatLE()
        return Vector2f.from(x, y)
    }

    fun writeVector2f(buffer: ByteBuf, vector2f: Vector2f) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(vector2f, "vector2f")
        buffer.writeFloatLE(vector2f.getX())
        buffer.writeFloatLE(vector2f.getY())
    }

    fun readVector3i(buffer: ByteBuf?): Vector3i {
        Preconditions.checkNotNull(buffer, "buffer")
        val x: Int = VarInts.readInt(buffer)
        val y: Int = VarInts.readInt(buffer)
        val z: Int = VarInts.readInt(buffer)
        return Vector3i.from(x, y, z)
    }

    fun writeVector3i(buffer: ByteBuf?, vector3i: Vector3i) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(vector3i, "vector3i")
        VarInts.writeInt(buffer, vector3i.getX())
        VarInts.writeInt(buffer, vector3i.getY())
        VarInts.writeInt(buffer, vector3i.getZ())
    }

    fun readBlockPosition(buffer: ByteBuf?): Vector3i {
        Preconditions.checkNotNull(buffer, "buffer")
        val x: Int = VarInts.readInt(buffer)
        val y: Int = VarInts.readUnsignedInt(buffer)
        val z: Int = VarInts.readInt(buffer)
        return Vector3i.from(x, y, z)
    }

    fun writeBlockPosition(buffer: ByteBuf?, blockPosition: Vector3i) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(blockPosition, "blockPosition")
        VarInts.writeInt(buffer, blockPosition.getX())
        VarInts.writeUnsignedInt(buffer, blockPosition.getY())
        VarInts.writeInt(buffer, blockPosition.getZ())
    }

    fun readByteRotation(buffer: ByteBuf): Vector3f {
        Preconditions.checkNotNull(buffer, "buffer")
        val pitch = readByteAngle(buffer)
        val yaw = readByteAngle(buffer)
        val roll = readByteAngle(buffer)
        return Vector3f.from(pitch, yaw, roll)
    }

    fun writeByteRotation(buffer: ByteBuf, rotation: Vector3f) {
        Preconditions.checkNotNull(buffer, "buffer")
        Preconditions.checkNotNull(rotation, "rotation")
        writeByteAngle(buffer, rotation.getX())
        writeByteAngle(buffer, rotation.getY())
        writeByteAngle(buffer, rotation.getZ())
    }

    fun readByteAngle(buffer: ByteBuf): Float {
        Preconditions.checkNotNull(buffer, "buffer")
        return buffer.readByte() * (360f / 256f)
    }

    fun writeByteAngle(buffer: ByteBuf, angle: Float) {
        Preconditions.checkNotNull(buffer, "buffer")
        buffer.writeByte((angle / (360f / 256f)) as Byte.toInt())
    }

    /*
        Helper array serialization
     */
    fun <T> readArray(
        buffer: ByteBuf?,
        array: MutableCollection<T>,
        function: BiFunction<ByteBuf?, BedrockPacketHelper?, T>
    ) {
        val length: Int = VarInts.readUnsignedInt(buffer)
        for (i in 0 until length) {
            array.add(function.apply(buffer, this))
        }
    }

    fun <T> writeArray(
        buffer: ByteBuf?,
        array: Collection<T>,
        consumer: TriConsumer<ByteBuf?, BedrockPacketHelper?, T>
    ) {
        VarInts.writeUnsignedInt(buffer, array.size)
        for (`val` in array) {
            consumer.accept(buffer, this, `val`)
        }
    }

    fun <T> readArray(
        buffer: ByteBuf?, array: MutableCollection<T>, session: BedrockSession?,
        function: TriFunction<ByteBuf?, BedrockPacketHelper?, BedrockSession?, T>
    ) {
        val length: Int = VarInts.readUnsignedInt(buffer)
        for (i in 0 until length) {
            array.add(function.apply(buffer, this, session))
        }
    }

    fun <T> writeArray(
        buffer: ByteBuf?, array: Collection<T>, session: BedrockSession?,
        consumer: QuadConsumer<ByteBuf?, BedrockPacketHelper?, BedrockSession?, T>
    ) {
        VarInts.writeUnsignedInt(buffer, array.size)
        for (`val` in array) {
            consumer.accept(buffer, this, session, `val`)
        }
    }

    fun <T> readArray(
        buffer: ByteBuf?,
        array: Array<T>?,
        function: BiFunction<ByteBuf?, BedrockPacketHelper?, T>?
    ): Array<T> {
        val list: ObjectArrayList<T> = ObjectArrayList<T>()
        readArray<T>(buffer, list, function)
        return list.toArray<T>(array)
    }

    fun <T> writeArray(buffer: ByteBuf?, array: Array<T>, consumer: TriConsumer<ByteBuf?, BedrockPacketHelper?, T>) {
        VarInts.writeUnsignedInt(buffer, array.size)
        for (`val` in array) {
            consumer.accept(buffer, this, `val`)
        }
    }

    fun <T> readArray(
        buffer: ByteBuf?, array: Array<T>?, session: BedrockSession?,
        function: TriFunction<ByteBuf?, BedrockPacketHelper?, BedrockSession?, T>?
    ): Array<T> {
        val list: ObjectArrayList<T> = ObjectArrayList<T>()
        readArray<T>(buffer, list, session, function)
        return list.toArray<T>(array)
    }

    fun <T> writeArray(
        buffer: ByteBuf?, array: Array<T>, session: BedrockSession?,
        consumer: QuadConsumer<ByteBuf?, BedrockPacketHelper?, BedrockSession?, T>
    ) {
        VarInts.writeUnsignedInt(buffer, array.size)
        for (`val` in array) {
            consumer.accept(buffer, this, session, `val`)
        }
    }

    fun <T> readArrayShortLE(
        buffer: ByteBuf,
        array: MutableCollection<T>,
        function: BiFunction<ByteBuf?, BedrockPacketHelper?, T>
    ) {
        val length: Int = buffer.readUnsignedShortLE()
        for (i in 0 until length) {
            array.add(function.apply(buffer, this))
        }
    }

    fun <T> writeArrayShortLE(
        buffer: ByteBuf,
        array: Collection<T>,
        consumer: TriConsumer<ByteBuf?, BedrockPacketHelper?, T>
    ) {
        buffer.writeShortLE(array.size)
        for (`val` in array) {
            consumer.accept(buffer, this, `val`)
        }
    }

    /*
        Non-helper array serialization
     */
    fun <T> readArray(buffer: ByteBuf?, array: MutableCollection<T>, function: Function<ByteBuf?, T>) {
        val length: Int = VarInts.readUnsignedInt(buffer)
        for (i in 0 until length) {
            array.add(function.apply(buffer))
        }
    }

    fun <T> writeArray(buffer: ByteBuf?, array: Collection<T>, biConsumer: BiConsumer<ByteBuf?, T>) {
        VarInts.writeUnsignedInt(buffer, array.size)
        for (`val` in array) {
            biConsumer.accept(buffer, `val`)
        }
    }

    fun <T> readArray(buffer: ByteBuf?, array: Array<T>?, function: Function<ByteBuf?, T>?): Array<T> {
        val list: ObjectArrayList<T> = ObjectArrayList<T>()
        readArray<T>(buffer, list, function)
        return list.toArray<T>(array)
    }

    fun <T> writeArray(buffer: ByteBuf?, array: Array<T>, biConsumer: BiConsumer<ByteBuf?, T>) {
        VarInts.writeUnsignedInt(buffer, array.size)
        for (`val` in array) {
            biConsumer.accept(buffer, `val`)
        }
    }

    fun <T> readArrayShortLE(buffer: ByteBuf, array: MutableCollection<T>, function: Function<ByteBuf?, T>) {
        val length: Int = buffer.readUnsignedShortLE()
        for (i in 0 until length) {
            array.add(function.apply(buffer))
        }
    }

    fun <T> writeArrayShortLE(buffer: ByteBuf, array: Collection<T>, biConsumer: BiConsumer<ByteBuf?, T>) {
        buffer.writeShortLE(array.size)
        for (`val` in array) {
            biConsumer.accept(buffer, `val`)
        }
    }

    fun <T> readTag(buffer: ByteBuf?): T {
        try {
            NbtUtils.createNetworkReader(ByteBufInputStream(buffer)).use { reader -> return reader.readTag() }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun <T> writeTag(buffer: ByteBuf?, tag: T) {
        try {
            NbtUtils.createNetworkWriter(ByteBufOutputStream(buffer)).use { writer -> writer.writeTag(tag) }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun readItemUse(buffer: ByteBuf, packet: InventoryTransactionPacket, session: BedrockSession?) {
        packet.setActionType(VarInts.readUnsignedInt(buffer))
        packet.setBlockPosition(readBlockPosition(buffer))
        packet.setBlockFace(VarInts.readInt(buffer))
        packet.setHotbarSlot(VarInts.readInt(buffer))
        packet.setItemInHand(readItem(buffer, session))
        packet.setPlayerPosition(readVector3f(buffer))
        packet.setClickPosition(readVector3f(buffer))
    }

    fun writeItemUse(buffer: ByteBuf, packet: InventoryTransactionPacket, session: BedrockSession?) {
        VarInts.writeUnsignedInt(buffer, packet.getActionType())
        writeBlockPosition(buffer, packet.getBlockPosition())
        VarInts.writeInt(buffer, packet.getBlockFace())
        VarInts.writeInt(buffer, packet.getHotbarSlot())
        writeItem(buffer, packet.getItemInHand(), session)
        writeVector3f(buffer, packet.getPlayerPosition())
        writeVector3f(buffer, packet.getClickPosition())
    }

    fun readInventoryActions(
        buffer: ByteBuf?,
        session: BedrockSession?,
        actions: List<InventoryActionData?>?
    ): Boolean {
        this.readArray(buffer, actions, session, { buf, helper, aSession ->
            val source: InventorySource = helper.readSource(buf)
            val slot: Int = VarInts.readUnsignedInt(buf)
            val fromItem: ItemData = helper.readItem(buf, aSession)
            val toItem: ItemData = helper.readItem(buf, aSession)
            InventoryActionData(source, slot, fromItem, toItem)
        })
        return false
    }

    fun writeInventoryActions(
        buffer: ByteBuf?, session: BedrockSession?, actions: List<InventoryActionData?>?,
        hasNetworkIds: Boolean
    ) {
        this.writeArray(buffer, actions, session, { buf, helper, aSession, action ->
            helper.writeSource(buf, action.getSource())
            VarInts.writeUnsignedInt(buf, action.getSlot())
            helper.writeItem(buf, action.getFromItem(), aSession)
            helper.writeItem(buf, action.getToItem(), aSession)
        })
    }

    fun readSource(buffer: ByteBuf?): InventorySource {
        val type: InventorySource.Type = InventorySource.Type.byId(VarInts.readUnsignedInt(buffer))
        return when (type) {
            CONTAINER -> {
                val containerId: Int = VarInts.readInt(buffer)
                InventorySource.fromContainerWindowId(containerId)
            }
            GLOBAL -> InventorySource.fromGlobalInventory()
            WORLD_INTERACTION -> {
                val flag: InventorySource.Flag = InventorySource.Flag.values().get(VarInts.readUnsignedInt(buffer))
                InventorySource.fromWorldInteraction(flag)
            }
            CREATIVE -> InventorySource.fromCreativeInventory()
            NON_IMPLEMENTED_TODO -> {
                containerId = VarInts.readInt(buffer)
                InventorySource.fromNonImplementedTodo(containerId)
            }
            else -> InventorySource.fromInvalid()
        }
    }

    fun writeSource(buffer: ByteBuf?, inventorySource: InventorySource) {
        Objects.requireNonNull<Any>(inventorySource, "InventorySource was null")
        VarInts.writeUnsignedInt(buffer, inventorySource.getType().id())
        when (inventorySource.getType()) {
            CONTAINER, UNTRACKED_INTERACTION_UI, NON_IMPLEMENTED_TODO -> VarInts.writeInt(
                buffer,
                inventorySource.getContainerId()
            )
            WORLD_INTERACTION -> VarInts.writeUnsignedInt(buffer, inventorySource.getFlag().ordinal())
        }
    }

    fun readRecipeIngredient(buffer: ByteBuf?): ItemData {
        Objects.requireNonNull<ByteBuf>(buffer, "buffer is null")
        val id: Int = VarInts.readInt(buffer)
        if (id == 0) {
            // We don't need to read anything extra.
            return ItemData.AIR
        }
        val meta: Int = VarInts.readInt(buffer)
        val count: Int = VarInts.readInt(buffer)
        return ItemData.builder()
            .id(id)
            .damage(meta)
            .count(count)
            .build()
    }

    fun writeRecipeIngredient(buffer: ByteBuf?, item: ItemData) {
        Objects.requireNonNull<ByteBuf>(buffer, "buffer is null")
        Objects.requireNonNull<Any>(item, "item is null")
        VarInts.writeInt(buffer, item.getId())
        if (item.getId() === 0) {
            return
        }
        VarInts.writeInt(buffer, item.getDamage())
        VarInts.writeInt(buffer, item.getCount())
    }

    fun readPotionRecipe(buffer: ByteBuf?): PotionMixData {
        Objects.requireNonNull<ByteBuf>(buffer, "buffer is null")
        return PotionMixData(
            VarInts.readInt(buffer),
            VarInts.readInt(buffer),
            VarInts.readInt(buffer),
            VarInts.readInt(buffer),
            VarInts.readInt(buffer),
            VarInts.readInt(buffer)
        )
    }

    fun writePotionRecipe(buffer: ByteBuf?, data: PotionMixData) {
        Objects.requireNonNull<ByteBuf>(buffer, "buffer is null")
        Objects.requireNonNull<Any>(data, "data is null")
        VarInts.writeInt(buffer, data.getInputId())
        VarInts.writeInt(buffer, data.getInputMeta())
        VarInts.writeInt(buffer, data.getReagentId())
        VarInts.writeInt(buffer, data.getReagentMeta())
        VarInts.writeInt(buffer, data.getOutputId())
        VarInts.writeInt(buffer, data.getOutputMeta())
    }

    fun readContainerChangeRecipe(buffer: ByteBuf?): ContainerMixData {
        Objects.requireNonNull<ByteBuf>(buffer, "buffer is null")
        return ContainerMixData(
            VarInts.readInt(buffer),
            VarInts.readInt(buffer),
            VarInts.readInt(buffer)
        )
    }

    fun writeContainerChangeRecipe(buffer: ByteBuf?, data: ContainerMixData) {
        Objects.requireNonNull<ByteBuf>(buffer, "buffer is null")
        Objects.requireNonNull<Any>(data, "data is null")
        VarInts.writeInt(buffer, data.getInputId())
        VarInts.writeInt(buffer, data.getReagentId())
        VarInts.writeInt(buffer, data.getOutputId())
    }

    fun readCommandEnumConstraints(
        buffer: ByteBuf,
        enums: List<CommandEnumData?>,
        enumValues: List<String?>
    ): CommandEnumConstraintData {
        val valueIndex: Int = buffer.readIntLE()
        val enumIndex: Int = buffer.readIntLE()
        val constraints: Array<CommandEnumConstraintType> = readArray(
            buffer, arrayOfNulls<CommandEnumConstraintType>(0)
        ) { buf -> CommandEnumConstraintType.byId(buffer.readByte()) }
        return CommandEnumConstraintData(
            enumValues[valueIndex],
            enums[enumIndex],
            constraints
        )
    }

    fun writeCommandEnumConstraints(
        buffer: ByteBuf,
        data: CommandEnumConstraintData,
        enums: List<CommandEnumData?>,
        enumValues: List<String?>
    ) {
        buffer.writeIntLE(enumValues.indexOf(data.getOption()))
        buffer.writeIntLE(enums.indexOf(data.getEnumData()))
        writeArray(buffer, data.getConstraints(), { buf, constraint -> buf.writeByte(constraint.ordinal()) })
    }

    /**
     * Return true if the item id has a blockingTicks attached.
     * Only a shield should return true
     *
     * @param id ID of item
     * @param session BedrockSession which holds correct blockingId
     * @return true if reading/writing blockingTicks
     */
    fun isBlockingItem(id: Int, session: BedrockSession): Boolean {
        val blockingId: Int = session.hardcodedBlockingId.get()
        return id == blockingId
    }

    /**
     * In case of identifier being different in any version,
     * helper can be used to return correct identifier.
     * @return item identifier of shield.
     */
    val blockingItemIdentifier: String
        get() = "minecraft:shield"

    fun readExperiments(buffer: ByteBuf?, experiments: List<ExperimentData?>?) {
        throw UnsupportedOperationException()
    }

    fun writeExperiments(buffer: ByteBuf?, experiments: List<ExperimentData?>?) {
        throw UnsupportedOperationException()
    }

    protected fun registerStackActionRequestTypes() {
        throw UnsupportedOperationException()
    }

    fun getStackRequestActionTypeFromId(id: Int): StackRequestActionType {
        throw UnsupportedOperationException()
    }

    fun getIdFromStackRequestActionType(type: StackRequestActionType?): Int {
        throw UnsupportedOperationException()
    }

    fun readItemStackRequest(buffer: ByteBuf?, session: BedrockSession?): ItemStackRequest {
        throw UnsupportedOperationException()
    }

    fun writeItemStackRequest(buffer: ByteBuf?, session: BedrockSession?, request: ItemStackRequest?) {
        throw UnsupportedOperationException()
    }

    fun <O> readOptional(buffer: ByteBuf, emptyValue: O, function: Function<ByteBuf?, O>): O {
        return if (buffer.readBoolean()) {
            function.apply(buffer)
        } else emptyValue
    }

    fun <T> writeOptional(buffer: ByteBuf, isPresent: Predicate<T>, `object`: T, consumer: BiConsumer<ByteBuf?, T>) {
        Preconditions.checkNotNull(`object`, "object")
        Preconditions.checkNotNull(consumer, "read consumer")
        val exists = isPresent.test(`object`)
        buffer.writeBoolean(exists)
        if (exists) {
            consumer.accept(buffer, `object`)
        }
    }

    companion object {
        protected val log: InternalLogger = InternalLoggerFactory.getInstance(BedrockPacketHelper::class.java)
    }

    init {
        gameRuleTypes.defaultReturnValue(-1)
        registerEntityDataTypes()
        registerEntityData()
        registerEntityFlags()
        registerEntityEvents()
        registerGameRuleTypes()
        registerSoundEvents()
        registerLevelEvents()
        registerCommandParams()
        registerResourcePackTypes()
    }
}