package negativenumbers_7_1_1

import com.test.Item
import com.test.ItemService

class BootStrap {

    ItemService itemService

    def init = {
        itemService.save(new Item(name: 'Product A', stock: 1, itemValue: 12.34))
        itemService.save(new Item(name: 'Product B', stock: -1, itemValue: 12.34))
        itemService.save(new Item(name: 'Product C', stock: 1, itemValue: -12.34))
        itemService.save(new Item(name: 'Product D', stock: -1, itemValue: -12.34))
    }

    def destroy = {
    }

}
