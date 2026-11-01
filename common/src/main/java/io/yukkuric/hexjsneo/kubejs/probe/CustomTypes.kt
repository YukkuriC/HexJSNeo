package io.yukkuric.hexjsneo.kubejs.probe

import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import io.yukkuric.hexjsneo.casting.ArithmeticJS
import moe.wolfgirl.probejs.typescript.ClassPath
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar
import moe.wolfgirl.probejs.typescript.document.*
import moe.wolfgirl.probejs.typescript.document.base.KindAware
import moe.wolfgirl.probejs.typescript.document.base.Type
import moe.wolfgirl.probejs.typescript.document.types.special.NamespacedType

internal val HEXJS_TYPES: ClassPath = ClassPath.special("types.HexJSTypes")

internal val OPERATOR_KEY: NamespacedType = Types.namespaced(HEXJS_TYPES, "OperatorKey")
internal val IOTA_TYPE: NamespacedType = Types.namespaced(HEXJS_TYPES, "IotaTypeKey")

private val OPERATOR_KEY_VALUE: Type by lazy {
    Types.union(buildList {
        ArithmeticJS.BUILTIN_OPERATIONS.keys.forEach { add(Types.literal(it)) }
        add(Types.clazz(HexPattern::class.java))
        add(Types.raw("string & {}"))
    })
}
private val IOTA_TYPE_VALUE: Type by lazy {
    Types.union(buildList {
        HexIotaTypes.REGISTRY.keySet().forEach {
            add(Types.literal(it.toString()))
            add(Types.literal(it.path))
        }
        add(Types.clazz(IotaType::class.java).asMaybeGeneric())
        add(Types.raw("string & {}"))
    })
}

internal fun addCustomTypeDocs(registrar: DocumentRegistrar) {
    val builder = Members.clazz(HEXJS_TYPES).kind(KindAware.Kind.NAMESPACE)
    builder.member(TypeDecl(OPERATOR_KEY.asClassPath(), OPERATOR_KEY_VALUE, false))
    builder.member(TypeDecl(IOTA_TYPE.asClassPath(), IOTA_TYPE_VALUE, false))
    registrar.addDocument(HEXJS_TYPES, builder.build())
}
