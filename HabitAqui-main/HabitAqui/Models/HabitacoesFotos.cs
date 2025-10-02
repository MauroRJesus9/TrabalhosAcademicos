namespace HabitAqui.Models
{
    public class HabitacoesFotos
    {
        public int ID { get; set; }
        public string Nome { get; set; }
        public string TipoFicheiro { get; set; }
        public string Extencao { get; set; }
        public DateTime? CreatedOn { get; set; }


        //Uma propertyimage pode pertencer a uma Habitação
        public byte[]? Data { get; set; }
        public string? Motivo { get; set; } // Se é para Mostrar as Habitações ou para o depois do arrendamento.
        public int? HabitacoesId { get; set; }
        public Habitacoes Habitacoes { get; set; }

    }
}
