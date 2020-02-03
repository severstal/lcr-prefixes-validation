
export const REPORT_TYPE = {
    byPrefixes: {
        headers: ['id', 'price', 'operator', 'service', 'prefix', 'prefixInPrefZone', 'prefixZone', 'prefixZoneFromLcr', 'priceLcr', 'prefixZoneLcr', 'differenceType'],
        title: 'Сверка по префиксам',
        search: function (items, regExp) {
            const regexp = new RegExp(regExp, "i");
            items = items.filter(i =>
                regexp.test(i.price) ||
                regexp.test(i.operator) ||
                regexp.test(i.service) ||
                regexp.test(i.prefix) ||
                regexp.test(i.prefixInPrefZone) ||
                regexp.test(i.prefixZone) ||
                regexp.test(i.prefixZoneFromLcr) ||
                regexp.test(i.priceLcr) ||
                regexp.test(i.prefixZoneLcr));
            return items;
        },
    },
    
    byCount: {
        headers: ['id', 'prefixZoneItc', 'prefixZoneLcr', 'operator', 'service', 'countItc', 'countLcr'],
        title: 'Сверка по количеству',
        search: function (items, regExp) {
            const regexp = new RegExp(regExp, "i");
            items = items.filter(i =>
                regexp.test(i.prefixZoneItc) ||
                regexp.test(i.prefixZoneLcr) ||
                regexp.test(i.operator) ||
                regexp.test(i.service));
            return items;
        },
    }
};


