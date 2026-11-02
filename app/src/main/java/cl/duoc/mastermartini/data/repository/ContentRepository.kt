package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.model.BusinessType
import cl.duoc.mastermartini.data.model.Difficulty
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.data.model.LearnKind
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.ServiceItem
import cl.duoc.mastermartini.data.model.ServiceType

/**
 * Fuente de contenido de la app. Hoy entrega datos ficticios en memoria;
 * en la EFT se puede reemplazar por una API REST sin tocar las pantallas,
 * porque los ViewModels solo conocen esta interfaz.
 */
interface ContentRepository {
    fun learnItems(): List<LearnItem>
    fun learnItem(id: String): LearnItem?
    fun products(): List<Product>
    fun product(id: String): Product?
    fun productCategories(): List<String>
    fun services(): List<ServiceItem>
    fun service(id: String): ServiceItem?
    fun salesPhone(): String
    fun learnCategoriesFor(type: BusinessType): List<String>
}

class FakeContentRepository : ContentRepository {

    private val learn = listOf(
        LearnItem(
            id = "v-templado", kind = LearnKind.VIDEO, title = "Templado de chocolate paso a paso",
            category = "Chocolatería", duration = "Lectura · 5 min", difficulty = Difficulty.EASY, isNew = true,
            description = "Ficha de lectura sobre templado: la curva depende de la cobertura y del fabricante.",
            steps = listOf(
                "Consulta la curva de templado del fabricante: depende del tipo de cobertura.",
                "Usa un termómetro limpio y evita el contacto con agua.",
                "Comprueba la cristalización en una espátula antes de bañar las piezas.",
            ),
            productIds = listOf("p-cobertura", "p-manteca-cacao"),
        ),
        LearnItem(
            id = "v-laminado", kind = LearnKind.VIDEO, title = "Laminado de masa de hoja",
            category = "Panadería", duration = "Lectura · 6 min", difficulty = Difficulty.MEDIUM,
            description = "Ficha de lectura sobre pliegues simples y dobles para masas laminadas.",
            steps = listOf("Prepara el empaste.", "Envuelve la margarina.", "Realiza 3 vueltas dobles con reposo en frío."),
            productIds = listOf("p-margarina"),
        ),
        LearnItem(
            id = "v-helado", kind = LearnKind.VIDEO, title = "Base de helado italiano",
            category = "Heladería", duration = "Lectura · 5 min", difficulty = Difficulty.EASY,
            description = "Ficha introductoria al balance de ingredientes para una base de helado.",
            steps = listOf("Revisa la dosificación y proceso de la base comercial que utilizarás.", "Respeta el tratamiento térmico, enfriamiento y conservación definidos por el fabricante.", "Manteca con equipo adecuado. Esta ficha orientativa no sustituye una formulación validada."),
            productIds = listOf("p-base-helado", "p-crema"),
        ),
        LearnItem(
            id = "r-kuchen", kind = LearnKind.RECIPE, title = "Torta decorada: guía de montaje",
            category = "Pastelería", duration = "45 min", difficulty = Difficulty.EASY,
            description = "Montaje didáctico de una torta sencilla usando un bizcocho previamente elaborado. Fotografía ilustrativa.",
            ingredients = listOf("1 bizcocho de vainilla de 18 cm, ya horneado y frío", "300 ml de crema apta para batir", "100 g de fruta limpia para decorar"),
            steps = listOf("Enfría el bizcocho y córtalo en capas uniformes.", "Bate la crema siguiendo las instrucciones de conservación y preparación del fabricante.", "Rellena y cubre con crema, decora con fruta y mantén refrigerado hasta el servicio."),
            productIds = listOf("p-crema", "p-brillo"),
        ),
        LearnItem(
            id = "r-croissant", kind = LearnKind.RECIPE, title = "Croissants de mantequilla",
            category = "Panadería", duration = "5 h aprox.", difficulty = Difficulty.HARD,
            description = "Receta didáctica orientativa de bollería laminada. Los tiempos dependen de la temperatura y la fermentación.",
            ingredients = listOf("500 g de harina de fuerza", "250 ml de leche", "55 g de azúcar", "10 g de sal", "7 g de levadura seca", "50 g de mantequilla para la masa", "250 g de mantequilla fría para laminar", "1 huevo para pincelar"),
            steps = listOf("Mezcla harina, leche, azúcar, sal, levadura y 50 g de mantequilla; amasa y enfría hasta que pueda estirarse sin romperse.", "Encierra los 250 g de mantequilla fría en la masa. Realiza tres pliegues simples, refrigerando entre cada vuelta para mantener las capas.", "Estira, corta triángulos, enrolla y deja fermentar hasta que aumenten claramente de volumen. Pincela con huevo.", "Hornea en horno precalentado a 190 °C aproximadamente 18–22 minutos, hasta dorar. Ajusta el proceso a tu receta y equipo."),
            productIds = emptyList(),
        ),
        LearnItem(
            id = "r-bombones", kind = LearnKind.RECIPE, title = "Frutillas bañadas en chocolate",
            category = "Chocolatería", duration = "25 min", difficulty = Difficulty.EASY,
            description = "Para 5 porciones. Adaptación resumida de Liberty Mendez, Good Food. Fotografía representativa.",
            ingredients = listOf("400 g de frutillas", "100 g de chocolate oscuro"),
            sourceUrl = "https://www.bbcgoodfood.com/recipes/chocolate-dipped-strawberries",
            steps = listOf("Lava y seca completamente las frutillas; la humedad dificulta la adherencia.", "Derrite el chocolate a baño María suave, en un bol resistente al calor sin tocar el agua; remueve hasta homogeneizar.", "Baña parcialmente las frutillas y deja cristalizar sobre papel apto para alimentos. Conserva refrigerado y consume pronto."),
            productIds = listOf("p-cobertura"),
        ),
        LearnItem(
            id = "r-galletas", kind = LearnKind.RECIPE, title = "Galletas con chocolate para vitrina",
            category = "Galletas", duration = "40 min", difficulty = Difficulty.EASY,
            description = "Para 10 galletas. Adaptación resumida de Sophie Godwin, Good Food. Contiene gluten, huevo y leche. Fotografía representativa.",
            ingredients = listOf("120 g de mantequilla blanda", "75 g de azúcar rubia y 75 g de azúcar fina", "1 huevo mediano", "1 cucharadita de vainilla", "180 g de harina", "½ cucharadita de bicarbonato", "150 g de chocolate picado", "¼ cucharadita de sal"),
            sourceUrl = "https://www.bbcgoodfood.com/recipes/chocolate-chunk-cookies",
            steps = listOf("Precalienta a 180 °C (160 °C con ventilador). Prepara dos bandejas con papel para hornear.", "Bate mantequilla y azúcares; incorpora huevo y vainilla. Añade harina, bicarbonato, sal y chocolate.", "Divide en diez porciones separadas. Hornea 10–12 minutos, hasta que los bordes estén firmes. Deja reposar en la bandeja antes de pasar a una rejilla."),
            productIds = listOf("p-margarina"),
        ),
        LearnItem(
            id = "c-hoja", kind = LearnKind.COURSE, title = "Curso: masas de hoja",
            category = "Panadería", duration = "3 módulos", difficulty = Difficulty.MEDIUM,
            description = "Curso de ejemplo con lectura y seguimiento local. Sin certificación.",
            steps = listOf(
                "Materias grasas. Compara la ficha técnica de una grasa para laminado con una grasa para batido. Identifica su rango de trabajo y conservación. Actividad: anota dos diferencias antes de completar el módulo.",
                "Empaste. Busca consistencia similar entre la masa y la materia grasa; si una rompe a la otra, revisa la temperatura antes de continuar. Actividad: dibuja cómo cubrirías el empaste sin dejar zonas abiertas.",
                "Vueltas y reposo. Registra cada pliegue y deja reposar la masa según la receta elegida. El reposo facilita el estirado y evita forzar capas. Actividad: prepara una hoja de control de tiempos, temperatura y vueltas."
            ),
            productIds = listOf("p-margarina"),
        ),
        LearnItem(
            id = "c-vitrina", kind = LearnKind.COURSE, title = "Curso: pastelería de vitrina",
            category = "Pastelería", duration = "3 módulos", difficulty = Difficulty.MEDIUM,
            description = "Montaje, glaseados y conservación de tortas para vitrina.",
            steps = listOf(
                "Bizcochos. Trabaja con una receta comprobada, pesa ingredientes y deja enfriar antes de cortar. Actividad: escribe tu lista de preparación y los utensilios que necesitas para nivelar capas uniformes.",
                "Rellenos. Consulta conservación y alérgenos de cada ingrediente. Mantén la cadena de frío cuando corresponda y no mezcles lotes sin registrar. Actividad: diseña una etiqueta con preparación, conservación y alérgenos.",
                "Decoración. Planifica el acabado antes del montaje. Aplica una capa inicial, estabiliza y termina con utensilios limpios y adornos alimentarios. Actividad: dibuja una torta y explica cómo la transportarías sin dañar el acabado."
            ),
            productIds = listOf("p-crema", "p-brillo"),
        ),
        LearnItem(
            id = "r-ganache", kind = LearnKind.RECIPE, title = "Ganache de chocolate para relleno",
            category = "Chocolatería", duration = "20 min + frío", difficulty = Difficulty.EASY,
            description = "Preparación doméstica de ejemplo para rellenos y coberturas. La foto es ilustrativa.",
            ingredients = listOf("200 g de chocolate de repostería", "100 ml de crema apta para calentar"),
            steps = listOf("Pica el chocolate y colócalo en un recipiente resistente al calor.",
                "Calienta la crema hasta que esté muy caliente, sin dejarla hervir intensamente.",
                "Vierte sobre el chocolate, espera un minuto y mezcla hasta integrar. Refrigera según el uso previsto."),
            productIds = listOf("p-cobertura", "p-crema"),
        ),
        LearnItem(
            id = "r-copa-helado", kind = LearnKind.RECIPE, title = "Copa de helado con chocolate",
            category = "Heladería", duration = "10 min", difficulty = Difficulty.EASY,
            description = "Montaje de servicio con helado ya elaborado; ejemplo académico de presentación.",
            ingredients = listOf("4 bolas de helado elaborado y conservado correctamente", "40 g de chocolate de repostería", "20 ml de crema o leche para salsa", "Fruta limpia al gusto"),
            steps = listOf("Enfría dos copas y mantén el helado en su temperatura de conservación.",
                "Funde el chocolate a baño María suave, agrega la crema y mezcla hasta obtener una salsa uniforme.",
                "Distribuye el helado, agrega la salsa templada y decora con fruta. Sirve inmediatamente."),
            productIds = listOf("p-cobertura"),
        ),
        LearnItem(
            id = "v-vitrina", kind = LearnKind.VIDEO, title = "Organización de una vitrina de pastelería",
            category = "Pastelería", duration = "Lectura · 6 min", difficulty = Difficulty.EASY,
            description = "Ficha técnica académica: orden, información de alérgenos y presentación de preparaciones.",
            steps = listOf("Agrupa productos por tipo y verifica que sus etiquetas se puedan leer.",
                "Consulta las condiciones de conservación de cada preparación y evita mezclar productos incompatibles.",
                "Registra fecha de preparación y alérgenos; revisa diariamente la limpieza del equipamiento."),
            productIds = listOf("p-brillo", "p-crema"),
        ),
        LearnItem(
            id = "v-horneo", kind = LearnKind.VIDEO, title = "Control del horneado de galletas",
            category = "Galletas", duration = "Lectura · 5 min", difficulty = Difficulty.EASY,
            description = "Ficha técnica académica sobre tamaño de porción, separación y control del color.",
            steps = listOf("Pesa porciones semejantes y distribúyelas en una bandeja con suficiente espacio.",
                "Precalienta el horno conforme a la receta elegida; controla la primera bandeja para ajustar el tiempo.",
                "Registra temperatura, minutos y resultado para repetir el proceso en futuros lotes."),
            productIds = listOf("p-margarina"),
        ),
        LearnItem(
            id = "c-chocolate", kind = LearnKind.COURSE, title = "Curso: fundamentos del chocolate",
            category = "Chocolatería", duration = "3 módulos", difficulty = Difficulty.EASY,
            description = "Curso didáctico de lectura con seguimiento local. No incluye certificación oficial.",
            steps = listOf("Tipos de chocolate: distingue porcentajes, ingredientes y aplicaciones a partir de fichas técnicas.",
                "Conservación: identifica condiciones de almacenamiento y evita humedad y cambios bruscos de temperatura.",
                "Aplicación: planifica una cobertura, identifica alérgenos y documenta el proceso elegido."),
            productIds = listOf("p-cobertura", "p-manteca-cacao"),
        ),
    )

    private val products = listOf(
        Product("p-crema", "Crema vegetal para batir", "Pastelería", listOf("1 L", "10 L"), "Alto rendimiento y estabilidad en vitrina."),
        Product("p-cobertura", "Cobertura semiamarga", "Pastelería", listOf("1 kg", "5 kg"), "Cobertura 54 % cacao para templar."),
        Product("p-margarina", "Margarina para hojaldre", "Panadería", listOf("10 kg"), "Plasticidad ideal para laminados."),
        Product("p-brillo", "Brillo neutro", "Pastelería", listOf("1 kg", "6 kg"), "Gel de terminación en frío."),
        Product("p-manteca-cacao", "Manteca de cacao", "Pastelería", listOf("500 g", "3 kg"), "Para templado y pistolado."),
        Product("p-mejorador", "Mejorador de masas", "Panadería", listOf("1 kg", "10 kg"), "Mayor volumen y miga suave."),
        Product("p-base-helado", "Base para helado italiano", "Gelatería", listOf("2 kg"), "Base neutra en polvo."),
        Product("p-pasta-pistacho", "Pasta de pistacho", "Gelatería", listOf("1 kg"), "Saborizante concentrado."),
        Product("p-salsa-bechamel", "Salsa bechamel lista", "Food service", listOf("1 kg", "5 kg"), "Lista para usar en casino."),
    )

    private val services = listOf(
        ServiceItem(
            "s-masterclass", ServiceType.EVENT, "Masterclass de heladería", "PRESENCIAL",
            "Santiago · Centro de innovación", dateLabel = "20 de noviembre de 2026 · 10:00 h", takenSeats = 12, totalSeats = 20,
        ),
        ServiceItem(
            "s-asesoria", ServiceType.ADVISORY, "Asesoría técnica con un chef", "ASESORÍA",
            "30 min por videollamada o en tu local",
        ),
        ServiceItem(
            "s-curso-hoja", ServiceType.COURSE, "Curso: masas de hoja", "ONLINE · GRATIS",
            "3 módulos a tu ritmo", progress = 0f, hasCertificate = false, learnId = "c-hoja",
        ),
        ServiceItem(
            "s-feria", ServiceType.EVENT, "Feria de innovación en panadería", "PRESENCIAL",
            "Concepción · Casa matriz regional", dateLabel = "5 de diciembre de 2026 · 09:30 h", takenSeats = 37, totalSeats = 60,
        ),
        ServiceItem(
            "s-curso-vitrina", ServiceType.COURSE, "Curso: pastelería de vitrina", "ONLINE · GRATIS",
            "3 módulos a tu ritmo", progress = 0f, hasCertificate = false, learnId = "c-vitrina",
        ),
    )

    override fun learnItems() = learn
    override fun learnItem(id: String) = learn.firstOrNull { it.id == id }
    override fun products() = products
    override fun product(id: String) = products.firstOrNull { it.id == id }
    override fun productCategories() = products.map { it.category }.distinct()
    override fun services() = services
    override fun service(id: String) = services.firstOrNull { it.id == id }
    override fun salesPhone() = "+56226611700"

    override fun learnCategoriesFor(type: BusinessType): List<String> = when (type) {
        BusinessType.PASTELERIA -> listOf("Pastelería", "Chocolatería", "Galletas")
        BusinessType.PANADERIA -> listOf("Panadería", "Galletas")
        BusinessType.CHOCOLATERIA -> listOf("Chocolatería", "Pastelería")
        BusinessType.HELADERIA -> listOf("Heladería", "Chocolatería")
        BusinessType.RESTAURANTE -> listOf("Pastelería", "Panadería", "Heladería")
    }

}
