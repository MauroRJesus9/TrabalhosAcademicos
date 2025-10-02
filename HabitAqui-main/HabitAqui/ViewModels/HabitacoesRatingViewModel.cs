using HabitAqui.Models;

namespace HabitAqui.ViewModels
{
    public class HabitacoesRatingViewModel{

        public int HabitacaoRatingId { get; set; }

        public string Comentario { get; set; }

        private int HabitacaoId { get; set; }
        public string Habitacao { get; set; }
        public Arrendamentos Arrendamentos { get; set;}

    }
}
