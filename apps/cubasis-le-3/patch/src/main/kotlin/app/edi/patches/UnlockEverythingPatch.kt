package app.edi.patches

import app.reseam.patch.CompatiblePackage
import app.reseam.patch.after
import app.reseam.patch.patch
import app.reseam.patch.klass
import app.reseam.patch.method

/*
Modifies the following method from

.method static bridge synthetic -$$Nest$mshopReceivedPrices(Lcom/steinberg/cubasisle3/AndroidActivity;Ljava/util/List;Ljava/util/List;)V
    .locals 0
    invoke-direct {p0, p1, p2}, Lcom/steinberg/cubasisle3/AndroidActivity;->shopReceivedPrices(Ljava/util/List;Ljava/util/List;)V
    return-void
.end method

to

.method static bridge synthetic -$$Nest$mshopReceivedPrices(Lcom/steinberg/cubasisle3/AndroidActivity;Ljava/util/List;Ljava/util/List;)V
    .locals 1
    invoke-direct {p0, p1, p2}, Lcom/steinberg/cubasisle3/AndroidActivity;->shopReceivedPrices(Ljava/util/List;Ljava/util/List;)V
    const/4 v0, 0x0
    invoke-direct {p0, p1, v0}, Lcom/steinberg/cubasisle3/AndroidActivity;->shopPurchasedProducts(Ljava/util/List;Z)V
    return-void
.end method

The outcome is that a new call to shopPurchasedProducts(productSkuList, false) is made after announcing the prices
This marks the products as purchased
The second parameter ("false") means that the products were purchased now, not restored from an earlier purchase
 */

val androidActivity = klass("com.steinberg.cubasisle3.AndroidActivity")

val shopReceivedPricesBridge = androidActivity.method("-\$\$Nest\$mshopReceivedPrices")
val shopPurchasedProducts = androidActivity.method("shopPurchasedProducts")

@Suppress("unused")
val unlockEverything = patch("Unlock everything") {
    description("Unlock all in-app purchases, including the full feature set. One visit in the in-app store is necessary for the unlock to happen.")
    compatibleWith(CompatiblePackage(
        name = "com.steinberg.cubasisle3",
        versions = listOf("3.8.6"),
    ))

    execute {
        val shopReceivedPricesBridgeMethod = shopReceivedPricesBridge.method

        shopReceivedPricesBridge.after {
            thisObject.call(shopPurchasedProducts, param(1), bool(true))
        }
    }

//    An alternative, more verbose approach:
/*
    execute {
        val shopReceivedPricesBridgeMethod = shopReceivedPricesBridge.method

        val returnInstructionIndex = shopReceivedPricesBridgeMethod.indexOfFirstInstruction {
            opcode == Opcode.RETURN_VOID
        } ?: error("\"return-void\" instructions not found")

        // Parameter registers: p0 = this, p1 = the product sku List.
        val paramBase = shopReceivedPricesBridgeMethod.registersSize - shopReceivedPricesBridgeMethod.insSize
        val p0 = paramBase
        val p1 = paramBase + 1

        // A register free at the insertion point, for the boolean argument.
        val boolReg = shopReceivedPricesBridgeMethod.findFreeRegister(returnInstructionIndex)

        // The call passes 3 registers; make sure the frame allows it.
        shopReceivedPricesBridgeMethod.ensureOutsSize(3)

        // Inserting at an index places the instructions before the instruction currently at that index
        shopReceivedPricesBridgeMethod.insertInstructions(
            returnInstructionIndex,
            buildInstructions {
                constInt(boolReg, 1)
                invokeDirect(
                    owner = "Lcom/steinberg/cubasisle3/AndroidActivity;",
                    name = "shopPurchasedProducts",
                    proto = "(Ljava/util/List;Z)V",
                    p0, p1, boolReg,
                )
            },
        )
    }
*/
}